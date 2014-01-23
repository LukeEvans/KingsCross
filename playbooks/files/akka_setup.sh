#!/bin/sh

# Get home
cd

# install git
sudo apt-get -y install git

# install sbt
wget http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.deb
sudo dpkg -i sbt.deb
sudo apt-get update
sudo apt-get -y install sbt
rm sbt.deb

# install java
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get -y install oracle-java7-installer

# install deps
sudo apt-get -f -y install
sudo apt-get update

# reinstall after deps installed
sudo apt-get -y install sbt
sudo apt-get -y install oracle-java7-installer
