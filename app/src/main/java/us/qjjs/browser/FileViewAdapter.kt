package us.qjjs.browser

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.doAsync
import org.json.JSONException
import org.json.JSONObject
import us.qjjs.R
import us.qjjs.SettingsFile
import java.io.BufferedOutputStream
import java.io.File
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import javax.net.ssl.HttpsURLConnection
import kotlin.math.log10
import kotlin.math.pow


class FileViewAdapter(private val context: Context, private val files: ArrayList<File>) : RecyclerView.Adapter<FileViewAdapter.FileViewHolder>() {

	class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val fileName = itemView.findViewById<TextView>(R.id.file_name)
		val fileSize = itemView.findViewById<TextView>(R.id.file_size)
		val fileAuthor = itemView.findViewById<TextView>(R.id.file_author)

		val delete = itemView.findViewById<ImageButton>(R.id.file_delete)
		val edit = itemView.findViewById<ImageButton>(R.id.file_edit)
		val upload = itemView.findViewById<ImageButton>(R.id.file_upload)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.file_view, parent, false)
		return FileViewHolder(view)
	}

	override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
		holder.fileName.text = files[position].name
		holder.fileSize.text = "Size: " + readableFileSize(files[position].length())
		val jsonObj = JSONObject(files[position].readText())
		try {
			holder.fileAuthor.text = jsonObj.getString("scoutName")
		} catch (e: JSONException) {
			holder.fileAuthor.visibility = View.GONE
		}
		holder.delete.setOnClickListener {
			AlertDialog.Builder(context).apply {
				setTitle(R.string.dialog_delete)
				setMessage(R.string.dialog_delete_content)
				setPositiveButton(R.string.yes) { _, _ ->
					files.removeAt(position).delete()
					this@FileViewAdapter.notifyItemRemoved(position)
					this@FileViewAdapter.notifyItemRangeChanged(position, files.size)
				}
				setNegativeButton(R.string.no, null)
			}.show()
		}
		holder.upload.setOnClickListener {

			doAsync {
				val connection = URL(SettingsFile(context)["url"] as String).openConnection() as HttpURLConnection

				try {
					connection.apply {
						doOutput = true
						doInput = true
						connectTimeout = 5000
						requestMethod = "POST"
						setRequestProperty("Date", files[position].name)
					}
					BufferedOutputStream(connection.outputStream).apply {
						write(files[position].readBytes())
						flush()
						close()
					}
					connection.connect()

					if (String(connection.errorStream.readBytes()) == "received") {
						if (files[position].renameTo(File(context.filesDir, "synced"))) {
							files[position].delete()
						}
						files.removeAt(position)
						this@FileViewAdapter.notifyItemRemoved(position)
						this@FileViewAdapter.notifyItemRangeChanged(position, files.size)
					}
				} catch (e: Exception) {
					Log.e("app", e.toString())
				} finally {
					connection.disconnect()
				}
			}
		}
		Log.i("app", files[position].parentFile?.name ?: "no parent!!")
		if (files[position].parentFile?.name == "templates" || files[position].parentFile?.name == "synced") {
			holder.upload.isEnabled = false
		}

	}

	override fun getItemCount(): Int {
		return files.size
	}

	private fun readableFileSize(size: Long): String? {
		if (size <= 0) return "0"
		val units = arrayOf("B", "KB", "MB", "GB", "TB")
		val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
		return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
	}
}