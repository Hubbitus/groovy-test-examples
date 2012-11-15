#!/bin/env groovy

import java.awt.Desktop;

File f = new File ("/home/pasha/temp/groovy-test/test.xls");
String path = "/home/pasha/temp/groovy-test/test.xls";

        try {
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
                return;
            }
        } catch(Exception e) {
            log.err(e);
        }

        try {
            String cmd = "/usr/bin/xdg-open";
            if(new File(cmd).exists()) {
//                Runtime.getRuntime().exec(new String[] { cmd, path });
                Runtime.getRuntime().exec( (String[])[cmd, path] );
                return;
            }
        } catch(Exception e) {
            log.err(e);
        }

        try {
            String cmd = "/usr/bin/gnome-open";
            if(new File(cmd).exists()) {
//                Runtime.getRuntime().exec(new String[] { cmd, path });
                Runtime.getRuntime().exec( (String[])[cmd, path] );
                return;
            }
        } catch(Exception e) {
            log.err(e);
        }

        try {
            String cmd = "/usr/bin/kde-open";
            if(new File(cmd).exists()) {
//                Runtime.getRuntime().exec(new String[] { cmd, path });
                Runtime.getRuntime().exec( (String[])[cmd, path] );
                return;
            }
        } catch(Exception e) {
            log.err(e);
        }
