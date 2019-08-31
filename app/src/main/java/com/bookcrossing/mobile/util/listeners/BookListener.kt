package com.bookcrossing.mobile.util.listeners

interface BookListener {
  fun onBookSelected(bookKey: String)

  fun onBookReleased(bookKey: String)

  fun onBookAdd()
}