package unaj.subastaya.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/auction/{auctionId}/bid")
    @SendTo("/topic/auction/{auctionId}")
    public String bidUpdate(String message) {
        return message;
    }
}
