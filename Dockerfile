FROM ubuntu:17.04

MAINTAINER hirooka

# Package
RUN apt-get -y update
#RUN apt-get -y update && apt-get -y upgrade
#RUN apt-get -y dist-upgrade
RUN apt-get -y install build-essential git wget libasound2-dev autoconf libtool pcsc-tools pkg-config libpcsclite-dev unzip yasm

# x264
RUN cd /tmp && \
    wget http://download.videolan.org/pub/x264/snapshots/last_x264.tar.bz2 && \
    tar xjvf last_x264.tar.bz2 && \
    cd x264-snapshot-* && \
    ./configure --enable-shared && \
    make && \
    make install

# FFmpeg
RUN cd /tmp && \
    wget https://ffmpeg.org/releases/ffmpeg-snapshot.tar.bz2 && \
    tar jxvf ffmpeg-snapshot.tar.bz2 && \
    cd ffmpeg && \
    ./configure --enable-gpl --enable-libx264 && \
    make -j8 && \
    make install

# Web camera (audio)
RUN mkdir /etc/modprobe.d
RUN touch /etc/modprobe.d/alsa-base.conf
RUN echo 'options snd slots=snd_usb_audio,snd_hda_intel' >> /etc/modprobe.d/alsa-base.conf
RUN echo 'options snd_usb_audio index=0' >> /etc/modprobe.d/alsa-base.conf
RUN echo 'options snd_hda_intel index=1' >> /etc/modprobe.d/alsa-base.conf

# recdvb
RUN cd /tmp && \
    git clone https://github.com/dogeel/recdvb && \
    cd recdvb && \
    chmod a+x autogen.sh && \
    ./autogen.sh && \
    ./configure && \
    make -j$(nproc) && \
    make install

# epgdump
RUN apt-get -y install cmake
RUN cd /tmp && \
    git clone https://github.com/Piro77/epgdump.git && \
    cd epgdump && \
    ./autogen.sh && \
    make && \
    make install

# Java
RUN apt-get -y install python-software-properties software-properties-common
RUN echo 'oracle-java8-installer shared/accepted-oracle-license-v1-1 select true' | debconf-set-selections
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get -y update
RUN apt-get -y install oracle-java8-installer
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# nginx
RUN cd /tmp && \
    apt-get -y install libpcre3-dev libpcre++-dev libssl-dev && \
    wget http://nginx.org/download/nginx-1.12.0.tar.gz && \
    tar zxvf nginx-*.tar.gz && \
    cd nginx-* && \
    ./configure --with-http_ssl_module --with-ipv6 --with-http_v2_module && \
    make && \
    make install
ADD docker/nginx/conf/nginx.conf /usr/local/nginx/conf/nginx.conf

# .sh for run both Spring Boot and nginx
ADD docker/startup.sh /startup.sh

RUN rm -rf /tmp/*

# chukasa
RUN mkdir -p /opt/chukasa/video
ADD ./build/libs/chukasa-0.0.1-SNAPSHOT.jar chukasa.jar

# locale
#RUN locale-gen en_US.UTF-8
#ENV LANG en_US.UTF-8
#ENV LANGUAGE en_US:en
#ENV LC_ALL en_US.UTF-8
RUN locale-gen ja_JP.UTF-8
ENV LANG ja_JP.UTF-8
ENV LANGUAGE ja_JP:ja
ENV LC_ALL ja_JP.UTF-8

RUN apt-get -y install tzdata
RUN echo "Asia/Tokyo" > /etc/timezone && dpkg-reconfigure tzdata

# run only Spring Boot
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/chukasa.jar"]

# run both Spring Boot and nginx
EXPOSE 80
ENTRYPOINT ["/bin/bash", "/startup.sh"]
