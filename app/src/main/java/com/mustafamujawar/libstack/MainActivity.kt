package com.mustafamujawar.libstack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mustafamujawar.libstack.data.CardData
import com.mustafamujawar.libstack.ui.theme.LibstackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibstackTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    LibstackApp()
                }
            }
        }
    }
}


@Composable
fun LibstackApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val intent = Intent(context, ScannerActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        val bookCard = CardData(title = R.string.scan_idcard, icon = R.drawable.id_card, onClick = {
//            intent.putExtra("com.mustafamujawar.libstack.scan", "book")
            Log.d("XD", "book card")
            context.startActivity(intent)
        })
        val idCard = CardData(title = R.string.scan_book, icon = R.drawable.book, onClick = {
//            intent.putExtra("com.mustafamujawar.libstack.scan", "id")
            Log.d("XD", "idcard")
            context.startActivity(intent)
        })


        ScanCard(card = bookCard)
        ScanCard(card = idCard)
    }
}

@Preview(showBackground = true)
@Composable
fun LibstackAppPreview() {
    LibstackTheme {
        LibstackApp()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCard(
    card: CardData, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(5.dp),
            shape = CardDefaults.elevatedShape,
            onClick = card.onClick
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(painter = painterResource(id = card.icon), contentDescription = null)
                Spacer(Modifier.padding(12.dp))
                Text(
                    text = stringResource(id = card.title),
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun ScanCardPreview() {
    LibstackTheme {
        ScanCard(CardData(R.string.scan_book, R.drawable.book, onClick = {}))
    }
}