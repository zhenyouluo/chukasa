# chukasa 

HTTP Live Streaming (HLS) server that distributes video file on demand or live stream of webcam or tuner to cross-platform.

[![Build Status](https://travis-ci.org/hirooka/chukasa.svg?branch=master)](https://travis-ci.org/hirooka/chukasa) [![Build Status](https://circleci.com/gh/hirooka/chukasa.png?style=shield)](https://circleci.com/gh/hirooka/chukasa)

チューナーの出力、Web カメラの映像とそのマイクの音声、任意のビデオファイルを「やや」リアルタイムにトランスコードしながら HLS で配信するウェブアプリケーションです。「やや」リアルタイムということで、10 秒くらいは遅延が発生します。

## Demo

#### Safari

![Safari demo](https://github.com/hirooka/animated_gif/blob/master/chukasa_web.gif)

#### iOS App

![iOS App demo](https://github.com/hirooka/animated_gif/blob/master/chukasa_ios.gif)

## Client

再生することができるかもしれないクライアントは下記の通りです。

* Safari の最新バージョン
* iOS App (chukasa-ios) [https://github.com/hirooka/chukasa-ios](https://github.com/hirooka/chukasa-ios)
* Apple TV 等、AirPlay でビデオを再生できるもの
* Microsoft Edge の最新バージョン
* Google Chrome の最新バージョン
* Internet Explorer 11
* Firefox の最新バージョン

## Getting Started with Docker

Java 8 と Docker がインストールされた Linux マシンがあれば、少しのコマンドを実行するだけでお試しできます。ビデオファイルをリアルタイムにトランスコードしながら HLS で配信し、それを再生してみましょう。

chukasa 用のディレクトリを作成します。video ディレクトリには何か適当なビデオファイルを突っ込んでおきます。

    sudo mkdir -p /opt/chukasa/video
    sudo chown -R $USER:$USER /opt/chukasa

プロジェクトを clone してビルドします。

    cd /tmp
    git clone https://github.com/hirooka/chukasa.git
    cd chukasa
    sed -i -e "s/localhost/mongo/g" src/main/resources/application.properties
    sed -i -e "s/localhost/postgres/g" chukasa-auth/src/main/resources/application.yml
    ./gradlew build

Docker イメージをビルドします。Core i3 6100 で 30 分程度かかります。

yourName は何か適当に設定してください。

    docker build -t <yourName>/chukasa:0.0.1-SNAPSHOT .

MongoDB の Docker イメージを pull して実行します。

    docker pull mongo
    docker run --name some-mongo -d mongo

PostgreSQL の Docker イメージを pull して実行します。

    docker pull postgres
    docker run --name some-postgres -e POSTGRES_USER=chukasa -d postgres

わざわざ 2 種類のデータベースを使用している理由は実験のためです。

chukasa の Docker イメージを実行します。yourName は何か適当に設定してください。

    docker run --link some-mongo:mongo --link some-postgres:postgres --privileged --volume /dev/:/dev/ --volume /var/run/pcscd/pcscd.comm:/var/run/pcscd/pcscd.comm -v /opt/chukasa/video:/opt/chukasa/video -p 80:80 -v /etc/localtime:/etc/localtime:ro -it hirooka/chukasa:0.0.1-SNAPSHOT /bin/bash

ウェブブラウザでアプリケーションの IP アドレス、TCP:80 にアクセスしてみます。ログインするための Username は admin, Password は admin です。

## Run somewhere

Ubuntu 16.04.2 with NVENC, CentOS with QSV, Raspberry Pi 3 Model B with OpenMAX, Docker, AWS Elastic Beanstalk など、どこかで動くかもしれません。

|   | ... |
|:---:|:---:|
| Ubuntu 16.04.2 with NVENC | [構築手順](procedure/procedure_ubuntu_16_04_2_nvenc.txt) |
| CentOS 7.2 with QSV | [構築手順](procedure/procedure_centos_7_2_qsv.txt) |
| Raspberry Pi 3 Model B with OpenMAX | [構築手順](procedure/procedure_raspberry_pi_3_model_b.txt) |
| Docker | [構築手順](procedure/procedure_ubuntu_16_04_2_docker.txt) |
| AWS Elastic Beanstalk (Java) | 確認中 |
| AWS Elastic Beanstalk (Docker) | 確認中 |
| AWS Elastic Beanstalk (Multi-container Docker) | 確認中 |
