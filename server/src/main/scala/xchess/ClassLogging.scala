package xchess

import org.slf4j.{Logger, LoggerFactory}

trait ClassLogging {
  protected val log: Logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))
}
