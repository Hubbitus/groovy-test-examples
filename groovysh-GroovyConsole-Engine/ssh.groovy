#!/bin/env groovy

//@Grab('org.codehaus.groovy:groovy-all:2.4.6')
//@Grab('net.java.dev.jna:platform:3.5.2')
//@Grab('org.hidetake:groovy-ssh:1.6.0')
//@Grab('ch.qos.logback:logback-classic:1.1.2')
def ssh = org.hidetake.groovy.ssh.Ssh.newService()

// deploy.groovy
ssh.remotes {
	rpmRepo {
		host = 'pkgs.taskdata.work'
		user = 'rpmrepo'
//		identity = new File('/home/pasha/.ssh/id_rsa')
		password = '_password_'
	}
}

ssh.run {
	session(ssh.remotes.rpmRepo) {
		execute 'hostname'
	}
}
