package com.joba.hadoop.commons

import scala.language.reflectiveCalls

object Utils {

  def arm[R, Q](resource: R {def close(): Unit})(f: (R) => Q): Q = {
    try {
      f(resource)
    } finally {
      resource.close()
    }
  }
}
