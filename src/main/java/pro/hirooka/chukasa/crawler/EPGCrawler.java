package pro.hirooka.chukasa.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.configuration.ChukasaConfiguration;
import pro.hirooka.chukasa.domain.EPGResponseModel;
import pro.hirooka.chukasa.domain.LastEPGCrawlerExecuted;
import pro.hirooka.chukasa.domain.ProgramInformation;
import pro.hirooka.chukasa.service.ILastEPGCrawlerExecutedService;
import pro.hirooka.chukasa.service.IProgramTableService;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class EPGCrawler {

    private final ChukasaConfiguration chukasaConfiguration;
    private final IProgramTableService programTableService;
    private final ILastEPGCrawlerExecutedService lastEPGCrawlerExecutedService;

    private final String SPLIT_WORD = "/////,/////"; // TODO

    @Autowired
    public EPGCrawler(ChukasaConfiguration chukasaConfiguration, IProgramTableService programTableService, ILastEPGCrawlerExecutedService lastEPGCrawlerExecutedService){
        this.chukasaConfiguration = requireNonNull(chukasaConfiguration, "chukasaConfiguration");
        this.programTableService = requireNonNull(programTableService, "programTableService");
        this.lastEPGCrawlerExecutedService = requireNonNull(lastEPGCrawlerExecutedService, "lastEPGCrawlerExecutedService");
    }

    @PostConstruct
    public void init(){

        if(chukasaConfiguration.isEpgAccessOnBootEnabled()){
            LastEPGCrawlerExecuted lastEPGCrawlerExecuted = lastEPGCrawlerExecutedService.read(0);
            if(lastEPGCrawlerExecuted == null){
                getEPG();
            }else{
                Date date = new Date();
                long now = date.getTime();
                long last = lastEPGCrawlerExecuted.getDate();
                log.info("{}, {}", now, last);
                if(now - last > chukasaConfiguration.getEpgAccessOnBootIgnoreInterval()){
                    getEPG();
                }
            }
        }

//        LastEPGCrawlerExecuted lastEPGCrawlerExecuted = lastEPGCrawlerExecutedService.read(0);
//
//        if(lastEPGCrawlerExecuted != null) {
//            Date date = new Date();
//            long now = date.getTime();
//            long last = lastEPGCrawlerExecuted.getDate();
//            log.info("{}, {}", now, last);
//
//            if(chukasaConfiguration.isEpgAccessOnBootEnabled()) {
//                getEPG();
//            }
//        }else{
//            if(chukasaConfiguration.isEpgAccessOnBootEnabled()) {
//                getEPG();
//            }
//        }
    }

    @Scheduled(cron = "0 20 */3 * * *")
    void getEPG(){

        long begin = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        List<String> dateList = new ArrayList<>();
        for(int i = 0; i < chukasaConfiguration.getEpgDays(); i++){
            String date = simpleDateFormat.format(calendar.getTime());
            dateList.add(date);
            calendar.add(Calendar.DATE, 1);
        }

        List<String> areaList = new ArrayList<>();
        areaList.add("23");  // Tokyo
        areaList.add("24");  // Kanagawa
        areaList.add("27");  // Chiba
        areaList.add("29");  // Saitama
        areaList.add("bs1"); // BS

        for(String area : areaList){
            get(dateList, area);
        }

        long end = System.currentTimeMillis();
        log.info((end - begin) / 1000 + "s");

        //
        LastEPGCrawlerExecuted lastEPGCrawlerExecuted = lastEPGCrawlerExecutedService.read(0);
        if(lastEPGCrawlerExecuted == null){
            lastEPGCrawlerExecuted = new LastEPGCrawlerExecuted();
            lastEPGCrawlerExecuted.setUnique(0);
        }
        Date date = new Date();
        lastEPGCrawlerExecuted.setDate(date.getTime());
        lastEPGCrawlerExecutedService.update(lastEPGCrawlerExecuted);
        log.info("lastEPGCrawlerExecuted = {}", lastEPGCrawlerExecuted.getDate());
    }

    void get(List<String> dateList, String area) {

        List<EPGResponseModel> epgResponseModelList = new ArrayList<>();

        for(String date : dateList) {

            List<String> listStart = new ArrayList<>();
            String linkDetail = "";

            try {

                //URL url = new URL("http://tv.so-net.ne.jp/chart/23.action?head=201512100000&span=24");
                date = date + "0000";
                String epgURL = chukasaConfiguration.getEpgBaseUrl() + area + ".action?head=" + date + "&span=" + chukasaConfiguration.getEpgSpan();
                System.out.println(epgURL);

                URL url = new URL(epgURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");

                boolean flag = false;
                boolean flagProg = false;
                boolean flagNoProg = false;
                int chkSta = 0;

                String station = "";
                String ch = "";
                String infoRec = "";

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String str;
                while ((str = bufferedReader.readLine()) != null) {

                    //  from ... to ...

                    // start parsing
                    if (str.startsWith("</div><div id=\"chartColumn\" class=\"chartColumn\"")) {
                        flag = true;
                    }

                    // stop parsing
                    if (str.equals("<div class=\"chartOverlayColumn\" id=\"chartOverlay\"></div>")) {
                        flag = false;
                    }

                    // parse
                    if (flag) {

                        // station name start at title=
                        // and station name appears 3 times
                        // title="st1" ... title="st1" ... title="st2" ... title="st2" ... title="st3" ... title="st3" ... title="st1"...
                        // so, chkSta == 2
                        if (str.trim().startsWith("title=")) {
                            station = str.split("\"")[1];
                            ch = checkStation(station);
                            chkSta++;
                            if (chkSta == 2) {
                                chkSta = 0;
                                flagNoProg = false;
                            }
                        }

                        // program info : <div class="cell-schedule ...
                        if (str.trim().startsWith("<div class=\"cell-schedule")) {
                            // cell-genre- mo atte hajimete prog info
                            if (str.trim().startsWith("<div class=\"cell-schedule cell-genre-")) {
                                flagProg = true;

                                // tvk
                                if (area.equals("24") && !ch.equals("18")) { // Kanagawa, tvk only
                                    flagProg = false;
                                }

                                // chiba
                                if (area.equals("27") && !ch.equals("17")) { // Chiba, ChibaTV only
                                    flagProg = false;
                                }

                                // saitama
                                if (area.equals("29") && !ch.equals("19")) { // Saitama, Teretama only
                                    flagProg = false;
                                }

                                // genre
                                //String genre = str.split("cell-genre-")[1].split(" ")[0]; // ???
                                String genre = str.split("cell-genre-")[2].split(" ")[0];

                                // schedule : start time yyyyMMddhhmm
                                String schedule = str.split("system-cell-schedule-head-")[1].split(" ")[0].split("\"")[0];

                                // info Rec : output format
                                infoRec = ch + SPLIT_WORD + genre + SPLIT_WORD + schedule;

                                // save start time in arrayList
                                listStart.add(schedule);

                                //
                                flagNoProg = true;

                                // start <div class="cell-schedule
                                // but no prog info
                            } else {

                                // all station in tokyo, tvk in kanagawa, chibatv in chiba, teletama in saitama
                                if (area.equals("23") || (area.equals("24") && ch.equals("18")) || (area.equals("27") && ch.equals("17")) || (area.equals("29") && ch.equals("19"))) {

                                    if (flagNoProg && (chkSta == 1)) {

                                        //String strNoProg = listStart.get(listStart.size()-1);

                                        // start time of no prog info is end time of previous prog
                                        String schedule = getNoProgStartTime(linkDetail);

                                        String idPrefix = getIdPrefix(ch);

                                        // genre is 999999
                                        infoRec = ch + SPLIT_WORD + "999999" + SPLIT_WORD + schedule + SPLIT_WORD + idPrefix + schedule + SPLIT_WORD + "NoProgramInformation" + SPLIT_WORD;

                                        // write inforec to output (because flagProg is false)
                                        EPGResponseModel epgResponseModel = new EPGResponseModel();
                                        epgResponseModel.setCh(Integer.parseInt(ch));
                                        epgResponseModel.setGenre(999999);
                                        epgResponseModel.setBegin(Long.parseLong(schedule));
                                        epgResponseModel.setId(Long.parseLong(idPrefix + schedule));
                                        epgResponseModel.setTitle("NoProgramInformation");
                                        epgResponseModel.setSummary("");
                                        epgResponseModelList.add(epgResponseModel);
                                        infoRec = "";

                                    }
                                }
                            }
                        }


                        if (flagProg) {

                            // id
                            if (str.trim().startsWith("id=")) {
                                String id = str.trim().split("-")[1];
                                infoRec += SPLIT_WORD + id;
                            }

                            if (str.trim().endsWith("class=\"schedule-link\">")) {
                                // can get detail info
                            }

                            // title
                            if (str.trim().startsWith("<span class=\"schedule-title\">")) {
                                String titlePre = str.split("<span class=\"schedule-title\">")[1].split("</span>")[0];
                                String title = titlePre.replace("<wbr/>", "");
                                title = title.replace("&amp;", "and"); // &
                                title = title.replace("&lt;", "(");    // <
                                title = title.replace("&gt;", ")");    // >
                                title = title.replace("&quot;", "_");  // "
                                infoRec += SPLIT_WORD + title;
                            }

                            // detail
                            if (str.trim().endsWith("class=\"schedule-link\">")) {
                                linkDetail = str.split("\"")[1];
                            }

                            // summary
                            // last span can be started a new line, in that case summary is ""
                            // set flagProg to false here
                            //if(str.trim().startsWith("<span class=\"schedule-summary\">")){
                            if (str.trim().startsWith("<span class=\"schedule-summary\">") && str.trim().endsWith("</span>")) {
                                flagProg = false;
                                String summaryPre = str.split("<span class=\"schedule-summary\">")[1].split("</span>")[0];
                                String summary = summaryPre.replace("<wbr/>", "");
                                summary = summary.replace("&amp;", "and"); // &
                                summary = summary.replace("&lt;", "(");    // <
                                summary = summary.replace("&gt;", ")");    // >
                                summary = summary.replace("&quot;", "_");  // "
                                infoRec += SPLIT_WORD + summary;
                            } else if (str.trim().startsWith("<span class=\"schedule-summary\">")) {
                                flagProg = false;
                                infoRec += SPLIT_WORD + "NoSummay";
                            }

                            // after summary -> flagProg: false -> write infoRec
                            if (!flagProg) {
                                EPGResponseModel epgResponseModel = new EPGResponseModel();
                                epgResponseModel.setCh(Integer.parseInt(infoRec.split(SPLIT_WORD)[0]));
                                epgResponseModel.setGenre(Integer.parseInt(infoRec.split(SPLIT_WORD)[1]));
                                epgResponseModel.setBegin(Long.parseLong(infoRec.split(SPLIT_WORD)[2]));
                                epgResponseModel.setId(Long.parseLong(infoRec.split(SPLIT_WORD)[3]));
                                epgResponseModel.setTitle(infoRec.split(SPLIT_WORD)[4]);
                                epgResponseModel.setSummary(infoRec.split(SPLIT_WORD)[5]);
                                epgResponseModelList.add(epgResponseModel);
                                infoRec = "";
                            }

                        } // if flagProg

                    } // if flag

                } // while

                bufferedReader.close();
                httpURLConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

//        epgResponseModelList.forEach(epgResponseModel -> {
//            log.info(epgResponseModel.toString());
//        });

        List<ProgramInformation> programInformationList = new ArrayList<>();
        for(int i = 0; i < 999; i++) {
            final int ch = i;
            List<EPGResponseModel> filterdEPGResponseModelList = epgResponseModelList.stream().filter(epgResponseModel -> epgResponseModel.getCh() == ch).collect(Collectors.toList());
            Collections.sort(filterdEPGResponseModelList, (o1, o2) -> Long.compare(o1.getBegin(), o2.getBegin()));
            for (int j = 0; j < filterdEPGResponseModelList.size() - 2; j++) {
                EPGResponseModel epgResponseModel = filterdEPGResponseModelList.get(j);
                EPGResponseModel nextEPGResponseModel = filterdEPGResponseModelList.get(j + 1);
                log.debug(epgResponseModel.toString());
                ProgramInformation programInformation = new ProgramInformation();
                programInformation.setCh(epgResponseModel.getCh());
                programInformation.setId(epgResponseModel.getId());
                programInformation.setGenre(epgResponseModel.getGenre());
                programInformation.setBeginDate(Long.toString(epgResponseModel.getBegin()));
                programInformation.setEndDate(Long.toString(nextEPGResponseModel.getBegin()));
                programInformation.setBegin(epgResponseModel.getBegin());
                programInformation.setEnd(nextEPGResponseModel.getBegin());
//                long start = epgResponseModel.getBegin() - chukasaConfiguration.getRecorderStartMargin();
//                long stop = nextEPGResponseModel.getBegin() - chukasaConfiguration.getRecorderStopMargin();
//                long duration = stop - start;
//                programInformation.setStart(start);
//                programInformation.setStop(stop);
//                programInformation.setDuration(duration);
                programInformation.setTitle(epgResponseModel.getTitle());
                programInformation.setSummary(epgResponseModel.getSummary());
                programInformationList.add(programInformation);
                log.info(programInformation.toString());
                programTableService.create(programInformation);
            }
        }
        log.info("programInformationList.size() = {}", programInformationList.size());
    }

    static String getNoProgStartTime(String in) throws IOException{

        log.debug(in);

        String duration = "0";

        final String BASE_URL = "http://tv.so-net.ne.jp";
        String detailUrl = BASE_URL + in;
        log.debug("[detailUrl] " + detailUrl);

        URL url = new URL(detailUrl);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String str;
        while((str = br.readLine()) != null){
            //if(str.trim().endsWith("この時間帯の番組表</a></dd>")){
            if(str.trim().endsWith("分）")){
                log.debug(str);
                duration = str.split("分")[0].split("（")[1];
            }
        }
        log.debug(duration);

        int yyyy1 = Integer.parseInt(in.split("/")[2].substring(6, 10));
        int MM1 = Integer.parseInt(in.split("/")[2].substring(10, 12)) - 1;
        int dd1 = Integer.parseInt(in.split("/")[2].substring(12, 14));
        int hh1 = Integer.parseInt(in.split("/")[2].substring(14, 16));
        int mm1 = Integer.parseInt(in.split("/")[2].substring(16, 18 )) + Integer.parseInt(duration);
        Calendar cal1 = Calendar.getInstance();
        cal1.set(yyyy1, MM1, dd1, hh1, mm1);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
        String ret = sdf1.format(cal1.getTime());
        log.debug(ret);

        return ret;
    }

    String checkStation(String station){
        String physicalCh = "";
        if(station.equals("ＮＨＫ総合・東京")){
            physicalCh = "27";
        }else if(station.equals("ＮＨＫＥテレ１・東京")){
            physicalCh = "26";
        }else if(station.equals("日テレ")){
            physicalCh = "25";
        }else if(station.equals("テレビ朝日")){
            physicalCh = "24";
        }else if(station.equals("ＴＢＳ")){
            physicalCh = "22";
        }else if(station.equals("テレビ東京")){
            physicalCh = "23";
        }else if(station.equals("フジテレビ")){
            physicalCh = "21";
        }else if(station.equals("ＴＯＫＹＯ　ＭＸ１")){
            physicalCh = "20";
        }else if(station.equals("放送大学１")){
            physicalCh = "28";
        }else if(station.equals("ｔｖｋ")){
            physicalCh = "18";
        }else if(station.equals("テレ玉")){
            physicalCh = "19";
        }else if(station.equals("チバテレ")){
            physicalCh = "17";
        }else if(station.equals("ＮＨＫ ＢＳ１")){
            physicalCh = "101";
        }else if(station.equals("ＮＨＫ ＢＳプレミアム")){
            physicalCh = "103";
        }else if(station.equals("ＢＳ日テレ")){
            physicalCh = "141";
        }else if(station.equals("ＢＳ朝日")){
            physicalCh = "151";
        }else if(station.equals("ＢＳ-ＴＢＳ")){
            physicalCh = "161";
        }else if(station.equals("ＢＳジャパン")){
            physicalCh = "171";
        }else if(station.equals("ＢＳフジ")){
            physicalCh = "181";
        }else{

        }
        return physicalCh;
    } // checkStation()

    String getIdPrefix(String ch){
        String ret = "";
        if(ch.equals("27")){
            ret = "101024";
        }else if(ch.equals("26")){
            ret = "101032";
        }else if(ch.equals("25")){
            ret = "101040";
        }else if(ch.equals("24")){
            ret = "101064";
        }else if(ch.equals("22")){
            ret = "101048";
        }else if(ch.equals("23")){
            ret = "101072";
        }else if(ch.equals("21")){
            ret = "101056";
        }else if(ch.equals("20")){
            ret = "123608";
        }else if(ch.equals("28")){
            ret = "101088";
        }else if(ch.equals("18")){
            ret = "124632";
        }else if(ch.equals("19")){
            ret = "129752";
        }else if(ch.equals("17")){
            ret = "127704";
        }else if(ch.equals("101")){
            ret = "200101";
        }else if(ch.equals("103")){
            ret = "200103";
        }else if(ch.equals("141")){
            ret = "200141";
        }else if(ch.equals("151")){
            ret = "200151";
        }else if(ch.equals("161")){
            ret = "200161";
        }else if(ch.equals("171")){
            ret = "200171";
        }else if(ch.equals("181")){
            ret = "200181";
        }else{

        }
        return ret;
    } // getIdPrefix()
}
