package com.joba.hadoop.test;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import java.io.File;

/**
 * This class instantiates and runs the Hbase shell on your terminal. Place the hbase-site.xml on its classpath to pass
 * any hbase properties to it like the connection zookeeper quorum.
 * The proper syntax and commands for the shell can be found here §{https://learnhbase.net/2013/03/02/hbase-shell-commands/}
 *
 * @author jose batalheiro
 * @project hadoop-env
 */
public class HBaseShell {
    private final static String WINUTILS_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator + "hadoop-winutils";

    public static void main(String[] args) {
        ScriptingContainer jruby = new ScriptingContainer();

        System.setProperty("hbase.ruby.sources",System.getProperty("user.dir") + "/tests/hadoop-environment/src/main/hbase/hbase-branch-2.0/hbase-shell/src/main/ruby");
        System.setProperty("hadoop.home.dir", WINUTILS_DIRECTORY);
        jruby.runScriptlet(PathType.RELATIVE, "tests/hadoop-environment/src/main/hbase/hbase-branch-2.0/bin/hirb.rb");
    }
}