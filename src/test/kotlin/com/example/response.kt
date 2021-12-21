package com.example

import org.intellij.lang.annotations.Language


@Language("JSON")
val nestedResponse = """
  {
    "data": {
      "viewer": {
        "__typename": "Viewer",
        "author": {
          "__typename": "Author",
          "id": "author-id",
          "name": "author-name",
          "topBook": {
            "__typename": "Book",
            "id": "book-id",
            "name": "book-name",
            "author": {
              "__typename": "Author",
              "id": "author-id"
            }
          }
        }
      }
    }
  }
""".trimIndent()
