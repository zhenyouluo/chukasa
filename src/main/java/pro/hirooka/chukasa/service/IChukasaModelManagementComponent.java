package pro.hirooka.chukasa.service;

import pro.hirooka.chukasa.domain.ChukasaModel;

import java.util.List;

public interface IChukasaModelManagementComponent {
    ChukasaModel create(int adaptiveBitrateStreaming, ChukasaModel chukasaModel);
    List<ChukasaModel> get();
    ChukasaModel get(int adaptiveBitrateStreaming);
    ChukasaModel update(int adaptiveBitrateStreaming, ChukasaModel chukasaModel);
    void delete(int adaptiveBitrateStreaming);
    void deleteAll();
}
