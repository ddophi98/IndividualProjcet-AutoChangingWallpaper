package kr.co.ddophi.autochangingwallpaper.MainActivity

import android.net.Uri
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class UriTypeAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter?, value: Uri?) {
        value?.let{
            out?.value(it.toString())
        }?: run{
            out?.nullValue()
        }
    }

    override fun read(reader: JsonReader?): Uri {
        return if(reader?.peek() == JsonToken.NULL) {
            reader.nextNull()
            Uri.EMPTY
        }else{
            val uriString = reader?.nextString()
            uriString?.let {
                Uri.parse(it)
            }?: run{
                Uri.EMPTY
            }
        }
    }
}