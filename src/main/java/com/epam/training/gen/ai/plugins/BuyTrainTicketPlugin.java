package com.epam.training.gen.ai.plugins;

import java.util.ArrayList;
import java.util.List;

import com.epam.training.gen.ai.models.TicketInfo;
import com.epam.training.gen.ai.models.TrainInfo;
import com.epam.training.gen.ai.models.TrainSchedule;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuyTrainTicketPlugin {

    private List<TrainInfo> availableTrains = new ArrayList<>();

    public BuyTrainTicketPlugin() {
        availableTrains.add(new TrainInfo(1L, "Lviv - Kyiv", List.of("Lviv", "Zhytomyr", "Kyiv")));
        availableTrains.add(new TrainInfo(2L, "Kyiv - Lviv", List.of("Kyiv", "Zhytomyr", "Lviv")));
    }

    @DefineKernelFunction(name = "get_available_trains", description = "Returns a list of trains with available seats")
    public TrainSchedule getAvailableTrains() {
        return new TrainSchedule(availableTrains);
    }

    @DefineKernelFunction(name = "buy_train_ticket", description = "Proceeds with buying a ticket to a selected train")
    public TicketInfo getBingSearchUrl(
            @KernelFunctionParameter(description = "Train identifier", name = "trainId") Long trainId) {
        return new TicketInfo(4L, trainId);
    }
}
