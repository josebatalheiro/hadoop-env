package com.joba.hadoop.commons.logging

import org.slf4j.LoggerFactory

trait Loggable {

  lazy implicit val loggable: Loggable = this

  private lazy val logger = LoggerFactory.getLogger(getClass.getCanonicalName.stripSuffix("$"))

  def INFO(message: => String): Unit = if (logger.isInfoEnabled) {
    logger.info(message)
  }

  def DEBUG(message: => String): Unit = if (logger.isDebugEnabled) {
    logger.debug(message)
  }

  def DEBUG(exception: Throwable)(message: => String): Unit = if (logger.isDebugEnabled) {
    logger.debug(message, exception)
  }

  def TRACE(message: => String): Unit = if (logger.isTraceEnabled) {
    logger.trace(message)
  }

  def TRACE(exception: Throwable)(message: => String): Unit = if (logger.isTraceEnabled) {
    logger.trace(message, exception)
  }

  def WARNING(message: => String): Unit = if (logger.isWarnEnabled) {
    logger.warn(message)
  }

  def WARNING(exception: Throwable)(message: => String): Unit = if (logger.isWarnEnabled) {
    logger.warn(message, exception)
  }

  def ERROR(message: => String): Unit = if (logger.isErrorEnabled) {
    logger.error(message)
  }

  def ERROR(exception: Throwable)(message: => String): Unit = if (logger.isErrorEnabled) {
    logger.error(message, exception)
  }
}
