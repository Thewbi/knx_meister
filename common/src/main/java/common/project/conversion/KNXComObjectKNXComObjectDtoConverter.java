package common.project.conversion;

import api.conversion.Converter;
import api.project.KNXComObject;
import api.project.dto.KNXComObjectDto;

public class KNXComObjectKNXComObjectDtoConverter implements Converter<KNXComObject, KNXComObjectDto> {

    @Override
    public KNXComObjectDto convert(final KNXComObject knxComObject) {

        final KNXComObjectDto knxComObjectDto = new KNXComObjectDto();
        knxComObjectDto.setId(knxComObject.getId());
        knxComObjectDto.setNumber(knxComObject.getNumber());
        knxComObjectDto.setText(knxComObject.getText());
        knxComObjectDto.setKnxGroupAddress(knxComObject.getKnxGroupAddress().getGroupAddress());

        return knxComObjectDto;
    }

}
