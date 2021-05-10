package com.example.fileexplorer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InternalFragment: Fragment(), OnFileSelectedListener
{
    private lateinit var recyclerView: RecyclerView
    private lateinit var img_back: ImageView
    private lateinit var tv_pathHolder: TextView
    private lateinit var fileAdapter: FileAdapter
    private var fileList: ArrayList<File> = ArrayList()

    private lateinit var myView: View

    lateinit var storage: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView = inflater.inflate(R.layout.fragment_internal, container, false)

        tv_pathHolder = myView.findViewById(R.id.tv_pathHolder)
        img_back = myView.findViewById(R.id.img_back)

        storage = getStorage(arguments)

        runtimePermission()

        tv_pathHolder.text = storage.absolutePath

        return myView
    }

    private fun getStorage(arguments: Bundle?): File
    {
        var storage = File(System.getenv("EXTERNAL_STORAGE"))

        arguments?.let {
            if (arguments.getString("path") != null) {
                storage = File(arguments.getString("path"))
            }
        }

        return storage
    }

    private fun runtimePermission()
    {
        Dexter.withContext(context).withPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                displayFiles()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }).check()
    }

    private fun displayFiles()
    {
        recyclerView = myView.findViewById(R.id.recycler_internal)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        fileList.addAll(findFiles(storage))
        fileAdapter = FileAdapter(context!!, fileList, this)
        recyclerView.adapter = fileAdapter
    }

    private fun findFiles(file: File): ArrayList<File>
    {
        val arrayList: ArrayList<File> = ArrayList()
        val files: Array<File>? = file.listFiles()
        files?.let {
            for (singleFile: File in files) {
                if (singleFile.isDirectory && !singleFile.isHidden) {
                    arrayList.add(singleFile)
                }
            }

            for (singleFile: File in files) {
                if (FileAllowManager.isAllowedImage(singleFile.name)) {
                    arrayList.add(singleFile)
                }
            }
        }

        return arrayList
    }

    override fun onFileClicked(file: File)
    {
        if (file.isDirectory) {
            val bundle = Bundle()
            bundle.putString("path", file.absolutePath)
            val internalFragment = InternalFragment()
            internalFragment.arguments = bundle
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.fragment_container, internalFragment)
                .addToBackStack(null)
                .commit()
        } else {
            try {
                FileOpener.openFile(context!!, file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onFileLongClicked(file: File, position: Int)
    {
        val optionDialog = Dialog(context!!)
        optionDialog.setContentView(R.layout.option_dialog)
        optionDialog.setTitle("Select Options")
        val options: ListView = optionDialog.findViewById(R.id.list)
        options.adapter = OptionListAdapter()
        optionDialog.show()

        options.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, pos, id ->
                when (parent?.getItemAtPosition(pos)) {
                    "Details" -> {
                        val details = TextView(context)
                        val lastModified = Date(file.lastModified())
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(lastModified)
                        details.text = ("File name: " + file.name + "\n"
                                + "Size: " + Formatter.formatShortFileSize(context, file.length()) + "\n"
                                + "Path: " + file.absolutePath + "\n"
                                + "Last modified: " + date)

                        AlertDialog
                            .Builder(context)
                            .setTitle("Details")
                            .setView(details)
                            .setPositiveButton("OK"
                            ) { _, _ -> optionDialog.cancel() }
                            .create()
                            .show()
                    }
                    "Rename" -> {
                        val name = EditText(context)

                        AlertDialog
                            .Builder(context)
                            .setTitle("Rename file")
                            .setView(name)
                            .setPositiveButton("OK") { _, _ ->
                                val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf("."))
                                val newName = name.editableText.toString()
                                val current = File(file.absolutePath)
                                val destination = File(file.absolutePath.replace(file.name, newName) + extension)

                                if (current.renameTo(destination)) {
                                    fileList[position] = destination
                                    fileAdapter.notifyItemChanged(position)
                                    Toast.makeText(context, "Renamed!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Couldn't rename!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Cancel"){ _, _ ->
                                optionDialog.cancel()
                            }
                            .create()
                            .show()
                    }
                    "Delete" -> {
                        AlertDialog
                            .Builder(context)
                            .setTitle("Delete " + file.name + "?")
                            .setPositiveButton("Yes") { _, _ ->
                                file.delete()
                                fileList.removeAt(position)
                                fileAdapter.notifyDataSetChanged()
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("No") { _, _ ->
                                optionDialog.cancel()
                            }
                            .create()
                            .show()
                    }
                    "Share" -> {
                        val share = Intent()
                        share.action = Intent.ACTION_SEND
                        share.type = "image/jpeg"
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                        startActivity(Intent.createChooser(share, "Share " + file.name))
                    }
                }
            }
    }

    inner class OptionListAdapter: BaseAdapter()
    {
        private var options = arrayOf(
            "Details", "Rename", "Delete", "Share"
        )

        override fun getCount(): Int
        {
            return options.size
        }

        override fun getItem(position: Int): Any
        {
            return options[position]
        }

        override fun getItemId(position: Int): Long
        {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
        {
            val myView = layoutInflater.inflate(R.layout.option_layout, null)
            val txtOption: TextView = myView.findViewById(R.id.txt_option)
            val imgOption: ImageView = myView.findViewById(R.id.img_option)
            txtOption.text = options[position]
            when {
                options[position] == "Details" -> {
                    imgOption.setImageResource(R.drawable.ic_details)
                }
                options[position] == "Rename" -> {
                    imgOption.setImageResource(R.drawable.ic_edit)
                }
                options[position] == "Delete" -> {
                    imgOption.setImageResource(R.drawable.ic_delete)
                }
                options[position] == "Share" -> {
                    imgOption.setImageResource(R.drawable.ic_share)
                }
            }

            return myView
        }
    }
}