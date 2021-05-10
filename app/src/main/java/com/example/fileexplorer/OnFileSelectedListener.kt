package com.example.fileexplorer

import java.io.File

interface OnFileSelectedListener
{
    fun onFileClicked(file: File)

    fun onFileLongClicked(file: File)
}