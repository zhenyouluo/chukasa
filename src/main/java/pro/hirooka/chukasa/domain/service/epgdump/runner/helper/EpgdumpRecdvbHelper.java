package pro.hirooka.chukasa.domain.service.epgdump.runner.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.hirooka.chukasa.domain.model.epgdump.RecdvbBSModel;

@Slf4j
@Component
public class EpgdumpRecdvbHelper implements IEpgdumpRecdvbHelper {

    // TODO: add

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

                break;
            case 236:

                break;
            case 238:

                break;
            case 241:

                break;
            case 242:

                break;
            case 243:

                break;
            case 244:

                break;
            case 245:

                break;
            case 251:

                break;
            case 252:

                break;
            case 255:

                break;
            case 256:

                break;
            case 258:

                break;
            case 531:

                break;
            default:
                break;
        }
        return recdvbBSModel;
    }
}
