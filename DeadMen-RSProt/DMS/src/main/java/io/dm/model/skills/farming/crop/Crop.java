package io.dm.model.skills.farming.crop;

import io.dm.model.entity.player.PlayerCounter;
import io.dm.model.item.Item;

public interface Crop {

	int getSeed();

	int getLevelReq();

	long getStageTime();

	int getTotalStages();

	double getDiseaseChance(int compostType);

	double getPlantXP();

	int getContainerIndex();

	int getProduceId();

	double getHarvestXP();

	PlayerCounter getCounter();

	default Item getPayment() {
		return null;
	}

}
