# chukasa 

HTTP Live Streaming (HLS) server to deliver PT3, web camera and video file for OS X and iOS

[![Build Status](https://travis-ci.org/hirooka/chukasa.svg?branch=master)](https://travis-ci.org/hirooka/chukasa) [![Build Status](https://circleci.com/gh/hirooka/chukasa.png?style=shield)](https://circleci.com/gh/hirooka/chukasa)

# Description

PT3 の出力，Web カメラの映像とそのマイクの音声，任意のビデオファイルをリアルタイムにトランスコードしながら HLS で配信するウェブアプリケーションです．ストリーミングされたビデオは，Safari (OS X, iOS) や iOS App で再生することができます．

# Demo

## Safari

![Safari demo](https://github.com/hirooka/animated_gif/blob/master/chukasa_web.gif)

## iOS App

![iOS App demo](https://github.com/hirooka/animated_gif/blob/master/chukasa_ios.gif)

# Features

* PT3 の出力を「やや」リアルタイムに HLS で配信
* Web カメラの映像とそのマイクの音声を「やや」リアルタイムに HLS で配信
* 任意のビデオファイルを HLS で配信
* epgdump を使用した番組情報の取得 (EXPERIMENTAL)
* PT3 を使用した録画 (EXPERIMENTAL)
* PT3 を使用した追っかけ再生 (EXPERIMENTAL)

「やや」リアルタイムということで，10 - 15 秒くらいは遅延が発生します．

# Client

ストリーミングを再生することができるクライアントは下記の通りです．

* Safari (OX 11.10, iOS 9)
* iOS App (chukasa-ios) [https://github.com/hirooka/chukasa-ios](https://github.com/hirooka/chukasa-ios)
* tvOS (play video via AirPlay)

# Run chukasa

## Quick Start on Docker

Java 8 と Docker がインストールされた Linux マシンがあれば、少しのコマンドを実行するだけでお試しできます．

chukasa 用のディレクトリを作成します．video ディレクトリには何か適当なビデオファイルを突っ込んでおきます．

    sudo mkdir /opt/chukasa
    sudo chown $USER:$USER /opt/chukasa
    mkdir /opt/chukasa/video

プロジェクトを clone してビルドします．

    cd /tmp
    git clone https://github.com/hirooka/chukasa.git
    cd chukasa
    sed -i -e "s/system.quick-sync-video-enabled=true/system.quick-sync-video-enabled=false/g" src/main/resources/application.properties
    sed -i -e "s/spring.data.mongodb.host=localhost/spring.data.mongodb.host=mongo/g" src/main/resources/application.properties
    ./gradlew build

Docker イメージをビルドします．Core i3 6100 で 30 分程度かかります．

yourName は何か設定してください．

    docker build -t <yourName>/chukasa:0.0.1-SNAPSHOT .

MongoDB の Docker イメージを pull して実行します．

    docker pull mongo
    docker run --name some-mongo -d mongo

chukasa の Docker イメージを実行します．

yourName は何か設定してください．

    docker run --link some-mongo:mongo --privileged --volume /dev/:/dev/ --volume /var/run/pcscd/pcscd.comm:/var/run/pcscd/pcscd.comm -v /opt/chukasa/video:/opt/chukasa/video -p 80:80 -v /etc/localtime:/etc/localtime:ro -it <yourName>/chukasa:0.0.1-SNAPSHOT /bin/bash

## Intel Quick Sync Video (QSV)

QSV を使用した低負荷なストリーミングができます．

第 4 世代インテル Core プロセッサシリーズ (Haswell Refresh)，Intel Media Server Studio 2016, CentOS 7.1 の組み合わせでのみ動作を確認しています．

その環境を構築する手順の一例は，[こちら](procedure/procedure_centos_7_1_qsv.txt)．

## Run Anywhere

Ubuntu 16.04.1，Docker，CentOS with QSV，Raspberry Pi 3 Model B with Raspbian、AWS Elastic Beanstalk など，いろいろな環境で起動させることができます．(動作するとは言っていない)

|   | Streaming & Recording | Streaming only | Recording only |
|:---:|:---:|:---:|:---:|
| CentOS 7.1<br>with QSV | [構築手順](procedure/procedure_centos_7_1_qsv.txt) | N/A | N/A |
| Ubuntu 16.04.1<br>(Local) | [構築手順](procedure/procedure_ubuntu_16_04_1_local.txt) | N/A | N/A |
| Ubuntu 16.04.1<br>(Docker) | [構築手順](procedure/procedure_ubuntu_16_04_1_docker.txt) | N/A | N/A |
| AWS Elastic Beanstalk<br>(Java) | N/A | [構築手順](procedure/procedure_aws_elastic_beanstalk_jar.txt) | N/A |
| AWS Elastic Beanstalk<br>(Docker) | N/A | [構築手順](procedure/procedure_aws_elastic_beanstalk_docker_jar.txt) | N/A |
| AWS Elastic Beanstalk<br>(Multi-container Docker) | N/A | [構築手順](procedure/procedure_aws_elastic_beanstalk_multi_container_docker_jar_db.txt) | N/A |
| Raspberry Pi 3 Model B<br>+ Raspbian Jessie | N/A | N/A | [構築手順](procedure/procedure_raspberry_pi_3_raspbian_jessie.txt) |