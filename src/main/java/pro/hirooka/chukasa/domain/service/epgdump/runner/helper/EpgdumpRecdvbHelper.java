package pro.hirooka.chukasa.domain.service.epgdump.runner.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;

@Slf4j
@Component
public class EpgdumpRecdvbHelper implements IEpgdumpRecdvbHelper {

    // TODO: -> common tuner service

    @Override
    public RecdvbBSModel resovle(int logicalChannel) {
        RecdvbBSModel recdvbBSModel = new RecdvbBSModel();
        switch (logicalChannel){
            case 101:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs15");
                recdvbBSModel.setTsid("0x40f1");
                break;
            case 103:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs15");
                recdvbBSModel.setTsid("0x40f2");
                break;
            case 141:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs13");
                recdvbBSModel.setTsid("0x40d0");
                break;
            case 151:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs01");
                recdvbBSModel.setTsid("0x4010");
                break;
            case 161:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs15");
                recdvbBSModel.setTsid("0x40f1");
                break;
            case 171:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs03");
                recdvbBSModel.setTsid("0x4031");
                break;
            case 181:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs13");
                recdvbBSModel.setTsid("0x4d01");
                break;
            case 191:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs03");
                recdvbBSModel.setTsid("0x4030");
                break;
            case 192:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs05");
                recdvbBSModel.setTsid("0x4450");
                break;
            case 200:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs09");
                recdvbBSModel.setTsid("0x4091");
                break;
            case 201:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs07");
                recdvbBSModel.setTsid("0x4470");
                break;
            case 202:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs07");
                recdvbBSModel.setTsid("0x4470");
                break;
            case 211:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs09");
                recdvbBSModel.setTsid("0x4490");
                break;
            case 222:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs09");
                recdvbBSModel.setTsid("0x4092");
                break;
            case 231:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b2");
                break;
            case 232:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b2");
                break;
            case 233:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b2");
                break;
            case 234:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs19");
                recdvbBSModel.setTsid("0x4730");
                break;
            case 236:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs07");
                recdvbBSModel.setTsid("0x4671");
                break;
            case 238:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b0");
                break;
            case 241:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b1");
                break;
            case 242:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs19");
                recdvbBSModel.setTsid("0x4731");
                break;
            case 243:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs19");
                recdvbBSModel.setTsid("0x4732");
                break;
            case 244:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs21");
                recdvbBSModel.setTsid("0x4751");
                break;
            case 245:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs21");
                recdvbBSModel.setTsid("0x4752");
                break;
            case 251:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs23");
                recdvbBSModel.setTsid("0x4771");
                break;
            case 252:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs21");
                recdvbBSModel.setTsid("0x4750");
                break;
            case 255:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs23");
                recdvbBSModel.setTsid("0x4771");
                break;
            case 256:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs07");
                recdvbBSModel.setTsid("0x4672");
                break;
            case 258:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs23");
                recdvbBSModel.setTsid("0x4772");
                break;
            case 531:
                recdvbBSModel.setLoginalChannel(logicalChannel);
                recdvbBSModel.setName("bs11");
                recdvbBSModel.setTsid("0x46b2");
                break;
            default:
                break;
        }
        return recdvbBSModel;
    }
}
