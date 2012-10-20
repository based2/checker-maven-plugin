package org.github.based2.maven.plugin.checker;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public class MockLog implements Log {

  ConsoleLogger consoleLogger;

  MockLog() {
    consoleLogger = new ConsoleLogger(0, "test");

  }

  @Override
  public void debug(CharSequence content) {

    consoleLogger.debug(content.toString());

  }

  @Override
  public void debug(Throwable error) {

    consoleLogger.debug(error.getMessage());

  }

  @Override
  public void debug(CharSequence content, Throwable error) {

    consoleLogger.debug(error.getMessage(), error);

  }

  @Override
  public void error(CharSequence content) {

    consoleLogger.error(content.toString());

  }

  @Override
  public void error(Throwable error) {

    consoleLogger.error(error.getMessage());

  }

  @Override
  public void error(CharSequence content, Throwable error) {

    consoleLogger.error(error.getMessage(), error);

  }

  @Override
  public void info(CharSequence content) {

    consoleLogger.info(content.toString());

  }

  @Override
  public void info(Throwable error) {

    consoleLogger.info(error.getMessage());

  }

  @Override
  public void info(CharSequence content, Throwable error) {

    consoleLogger.info(error.getMessage(), error);

  }

  @Override
  public boolean isDebugEnabled() {

    return consoleLogger.isDebugEnabled();

  }

  @Override
  public boolean isWarnEnabled() {

    return consoleLogger.isWarnEnabled();

  }

  @Override
  public boolean isErrorEnabled() {

    // TODO Auto-generated method stub

    return false;

  }

  @Override
  public boolean isInfoEnabled() {

    // TODO Auto-generated method stub

    return false;

  }

  @Override
  public void warn(CharSequence arg0) {

    // TODO Auto-generated method stub

  }

  @Override
  public void warn(Throwable arg0) {

    // TODO Auto-generated method stub

  }

  @Override
  public void warn(CharSequence arg0, Throwable arg1) {

    // TODO Auto-generated method stub

  }

}