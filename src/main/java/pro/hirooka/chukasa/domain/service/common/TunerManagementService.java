package pro.hirooka.chukasa.domain.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.hirooka.chukasa.domain.model.common.Tuner;
import pro.hirooka.chukasa.domain.model.common.TunerStatus;
import pro.hirooka.chukasa.domain.model.common.enums.RecxxxDriverType;
import pro.hirooka.chukasa.domain.model.recorder.enums.ChannelType;
import pro.hirooka.chukasa.domain.service.common.ulitities.CommonUtilityService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TunerManagementService implements ITunerManagementService {

    final String DVB_DEVICE = "dvb";
    final String CHADEV_DEVICE = "pt3";

    private List<TunerStatus> tunerStatusList = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private CommonUtilityService commonUtilityService;

    @PostConstruct
    void init(){
        List<Tuner> tunerList = commonUtilityService.getTunerList();
        for(Tuner tuner : tunerList){
            TunerStatus tunerStatus = new TunerStatus();
            tunerStatus.setChannelType(tuner.getChannelType());
            tunerStatus.setDeviceName(tuner.getDeviceName());
            tunerStatus.setCanUse(true);
            tunerStatus.setIndex(Integer.parseInt(tuner.getDeviceName().substring(tuner.getDeviceName().length() - 1)));
            if(tuner.getDeviceName().contains(DVB_DEVICE)){
                tunerStatus.setRecxxxDriverType(RecxxxDriverType.DVB);
            }else if(tuner.getDeviceName().contains(CHADEV_DEVICE)){
                tunerStatus.setRecxxxDriverType(RecxxxDriverType.CHARDEV);
            }
            tunerStatusList.add(tunerStatus);
        }
        log.info("tunerStatusList = {}", tunerStatusList.toString());
    }

    @Override
    public TunerStatus create(TunerStatus tunerStatus) {
        return null;
    }

    @Override
    public List<TunerStatus> get() {
        return tunerStatusList;
    }

    @Override
    public List<TunerStatus> get(ChannelType channelType) {
        Predicate<TunerStatus> predicate = tunerStatus -> tunerStatus.getChannelType() == channelType;
        return tunerStatusList.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<TunerStatus> available(ChannelType channelType) {
        Predicate<TunerStatus> predicate = tunerStatus -> tunerStatus.getChannelType() == channelType && tunerStatus.isCanUse();
        return tunerStatusList.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public TunerStatus findOne(ChannelType channelType) {
        List<TunerStatus> tunerStatusList = available(channelType);
        return tunerStatusList.stream().findFirst().orElse(null);
    }

    @Override
    public TunerStatus get(String deviceName) {
        return null;
    }

    @Override
    public TunerStatus update(TunerStatus tunerStatus) {
        for(int i = 0; i < tunerStatusList.size(); i++) {
            if (tunerStatusList.get(i).getChannelType() == tunerStatus.getChannelType()
                    && tunerStatusList.get(i).getDeviceName().equals(tunerStatus.getDeviceName())) {
                tunerStatusList.get(i).setCanUse(tunerStatus.isCanUse());
            }
        }
        for(int i = 0; i < tunerStatusList.size(); i++) {
            if (tunerStatusList.get(i).getChannelType() == tunerStatus.getChannelType()
                    && tunerStatusList.get(i).getDeviceName().equals(tunerStatus.getDeviceName())) {
                return tunerStatusList.get(i);
            }
        }
        return null;
    }

    @Override
    public TunerStatus update(TunerStatus tunerStatus, boolean canUse) {
        for(int i = 0; i < tunerStatusList.size(); i++) {
            if (tunerStatusList.get(i).getChannelType() == tunerStatus.getChannelType()
                    && tunerStatusList.get(i).getDeviceName().equals(tunerStatus.getDeviceName())) {
                tunerStatusList.get(i).setCanUse(canUse);
            }
        }
        for(int i = 0; i < tunerStatusList.size(); i++) {
            if (tunerStatusList.get(i).getChannelType() == tunerStatus.getChannelType()
                    && tunerStatusList.get(i).getDeviceName().equals(tunerStatus.getDeviceName())) {
                return tunerStatusList.get(i);
            }
        }
        return null;
    }

    @Override
    public RecxxxDriverType getRecxxxDriverType() {
        for(TunerStatus tunerStatus : tunerStatusList){
            return tunerStatus.getRecxxxDriverType(); // TODO:
        }
        return null;
    }
}
