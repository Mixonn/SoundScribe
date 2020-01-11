#!/usr/bin/env bash

apt-get update
apt -y --force-yes install build-essential
curl https://code.soundsoftware.ac.uk/attachments/download/2588/vamp-plugin-sdk-2.9.0.tar.gz --output vamp.tar.gz
tar -zxvf vamp.tar.gz && rm vamp.tar.gz
apt -y --force-yes install libsndfile-dev
cd vamp-plugin-sdk-2.9.0
./configure
make
make install
cp libvamp-hostsdk.so /usr/lib/libvamp-hostsdk.so
cd ..
curl https://code.soundsoftware.ac.uk/attachments/download/1574/jvamp-1.3.tar.gz --output jvamp.tar.gz
tar -zxvf jvamp.tar.gz && rm jvamp.tar.gz
cd jvamp-1.3/
make
cd ..
mkdir pyin
cd pyin
curl https://code.soundsoftware.ac.uk/attachments/download/1461/pyin-v1.1-linux64.tar.gz --output pyin.tar.gz
tar -zxvf pyin.tar.gz && rm pyin.tar.gz
cp {pyin.cat,pyin.n3,pyin.so} /usr/local/lib/vamp

