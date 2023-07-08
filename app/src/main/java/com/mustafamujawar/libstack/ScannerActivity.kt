package com.mustafamujawar.libstack

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mustafamujawar.libstack.network.LoanRq
import com.mustafamujawar.libstack.network.apiService
import com.mustafamujawar.libstack.ui.theme.LibstackTheme
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class ScannerActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter> = arrayOf()
    private var techListsArray: Array<Array<String>> = arrayOf(arrayOf(""))

    //    private val type: String? = intent.getStringExtra("com.mustafamujawar.libstack.scan")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            Toast.makeText(this, "NFC Supported", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "NFC is not supported", Toast.LENGTH_SHORT).show()
        }
        if (nfcAdapter?.isEnabled == true) {
            Toast.makeText(this, "NFC Enabled", Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(this, this.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")    /* Handles all MIME based dispatches.
                                 You should specify only the ones that you need. */
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        intentFiltersArray = arrayOf(ndef)
        techListsArray = arrayOf(arrayOf(NfcA::class.java.name))

        setContent {
            LibstackTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    ScannerApp()
                }
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this, pendingIntent, intentFiltersArray, techListsArray
        )
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array.
                val toast = processNdefMessages(messages)
                Log.d("NFC DATA", toast)
                Log.d("NFC DATA", toast.length.toString())
                // clip for testing
                if (toast.contains(",")) {
                    if ((toast.split(",")[0] == "book")) {
                        ScannedValues.book = toast.split(",")[1]
                    } else {
                        ScannedValues.id = toast.split(",")[1]
                    }
                }


                Log.d("SCANNED VALUE", ScannedValues.book + " " + ScannedValues.id)
//                val clip: ClipData = ClipData.newPlainText("ndef message", toast)
//                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//                clipboard.setPrimaryClip(clip)
                if ((ScannedValues.book != "") && (ScannedValues.id != "")) {
                    val currentInstant: java.time.Instant = java.time.Instant.now()
                    val due_date: Long = currentInstant.toEpochMilli() + 604800000
                    val body = LoanRq(ScannedValues.id, ScannedValues.book, due_date)
                    sendRequest(body)
                    scope()
                    Log.d("POP UP", "enabled")
                }
            }
        }
    }


}

object ScannedValues {
    var book = ""
    var id = ""
}

val pop: MutableLiveData<Boolean> = MutableLiveData(false)
fun scope() {
    pop.postValue(true)
}


@JvmSuppressWildcards(suppress = true)
fun sendRequest(body: LoanRq = LoanRq("", "", 0)) {
    apiService.postLoan(body).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            // handle the response
            Log.d("HTTP RESPONSE", response.body().toString())
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // handle the failure
            Log.d("HTTP FAIL", call.toString())
        }
    })
}

fun processNdefMessages(msgs: List<NdefMessage>): String {
    var toast = ""
    msgs.forEach { ndef ->
        ndef.records.forEach { record ->
            record?.payload?.let { byteArray ->
                toast += String(byteArray)
            }
        }
    }

    // offset due to some weird bytes being read
    return toast.slice(3 until toast.length)
}

@Composable
fun ScannerApp() {

    val popin = pop as LiveData<Boolean>
    val popin2 = popin.observeAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (popin2.value == true) {
            ScanDialog(title = R.string.successful,
                desc = "Successfully loaned ${ScannedValues.book} to ${ScannedValues.id}",
                onClose = { pop.value = false })
        }


        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Tap the",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
            Text(
                text = "NFC Tag",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            )
            Text(
                text = "with device",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )


        }
    }
}

@Composable
private fun ScanDialog(
    @StringRes title: Int, desc: String, onClose: () -> Unit, modifier: Modifier = Modifier
) {
//    val activity = (LocalContext.current as Activity)

    AlertDialog(icon = {
        Icon(
            painter = painterResource(id = R.drawable.wave),
            contentDescription = "contactless icon"
        )
    },
        onDismissRequest = onClose,
        title = { Text(stringResource(title)) },
        text = { Text(desc) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = onClose
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {})
}


@Preview(showBackground = true)
@Composable
fun ScanAppPreview() {
    LibstackTheme {
        ScannerApp()
    }
}
