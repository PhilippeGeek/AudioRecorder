#!/bin/bash
cd $(dirname $0)
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-x86_64-w64-mingw32.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-x86_64-xuggle-darwin11.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-i686-pc-linux-gnu.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-x86_64-pc-linux-gnu.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-i686-w64-mingw32.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-arch-i386-xuggle-darwin11.jar
wget http://xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-5.4.jar
wget http://www.slf4j.org/dist/slf4j-1.7.2.tar.gz
tar xf slf4j-1.7.2.tar.gz
cp slf4j-1.7.2/slf4j-api-1.7.2.jar .
cp slf4j-1.7.2/slf4j-ext-1.7.2.jar .
cp slf4j-1.7.2/slf4j-simple-1.7.2.jar .
rm -rf slf4j-1.7.2
rm -rf slf4j-1.7.2.tar.gz
