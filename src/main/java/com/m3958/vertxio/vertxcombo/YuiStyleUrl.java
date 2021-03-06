package com.m3958.vertxio.vertxcombo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.logging.Logger;


public class YuiStyleUrl extends UrlStyle {

  // http://yui.yahooapis.com/combo?3.14.1/event-mouseenter/event-mouseenter-min.js&3.14.1/event-hover/event-hover-min.js

  public YuiStyleUrl(FileSystem fileSystem, Logger logger, Path comboDiskRootPath) {
    super(fileSystem, logger, comboDiskRootPath);
  }

  public YuiStyleUrl(FileSystem fileSystem, Logger logger, String comboDiskRootPath) {
    super(fileSystem, logger, comboDiskRootPath);
  }

  @Override
  public ExtractFileResult extractFiles(String url) {
    url = sanitizeUrl(url);
    int qidx = url.indexOf('?');
    // /combo/version?
    String version = url.substring(0, qidx);
    String fnPart = url.substring(qidx + 1);
    if (fnPart.startsWith("&")) {
      fnPart = fnPart.substring(1);
    }
    String[] fns = fnPart.split("&");

    char fsep = File.separatorChar;
    char unwantedFsep = fsep == '/' ? '\\' : '/';

    Path[] sanitizedPathes = new Path[fns.length];

    FN_LOOP: for (int i = 0; i < fns.length; i++) {
      String fn = fns[i];
      if (fn.isEmpty()) {
        continue FN_LOOP;
      }
      fn = fn.replace(unwantedFsep, fsep);
      if (fn.charAt(0) == fsep) {
        fn = fn.substring(1);
      }
      sanitizedPathes[i] = comboDiskRootPath.resolve(fn);
    }


    if (testExists(sanitizedPathes)) {
      return new ExtractFileResult(sanitizedPathes, version, url).setMimeType();
    } else {
      return new ExtractFileResult(ExtractFileResult.ResultStatus.FILE_NOT_FOUND);
    }

  }

  @Override
  public String generateRandomUrl(String pattern, int number, String version) {
    RandomFileFinder rff = new RandomFileFinder(comboDiskRootPath, pattern, number);
    StringBuilder sb;
    if (version == null || version.isEmpty()) {
      sb = new StringBuilder("/combo?");
    } else {
      sb = new StringBuilder("/combo/").append(version).append("?");
    }
    try {
      List<Path> selected = rff.selectSome();
      for (Path p : selected) {
        sb.append(p.toString().replace('\\', '/')).append('&');
      }
      if (sb.charAt(sb.length() - 1) == '&') {
        sb = sb.deleteCharAt(sb.length() - 1);
      }
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
