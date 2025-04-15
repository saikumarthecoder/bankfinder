package com.bank.bankfinder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BankController {

    private final String GOOGLE_API_KEY = "AIzaSyAgUI6eDgQvNlBBp55MPS_pCwwga0HLkog"; // Replace with your real key

    @GetMapping("/nearby-banks")
    public ResponseEntity<List<String>> getNearbyBanks(@RequestParam String zipcode) {
        String location = getCoordinatesFromZip(zipcode);
        if (location == null) {
            return ResponseEntity.badRequest().body(List.of("Invalid Zipcode"));
        }

        List<String> banks = getNearbyBanksFromGoogle(location);
        return ResponseEntity.ok(banks);
    }

    private String getCoordinatesFromZip(String zip) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + zip + "&key=" + GOOGLE_API_KEY;
        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("results") == null) return null;

        List results = (List) response.get("results");
        if (results.isEmpty()) return null;

        Map geometry = (Map) ((Map) results.get(0)).get("geometry");
        Map location = (Map) geometry.get("location");
        return location.get("lat") + "," + location.get("lng");
    }

    private List<String> getNearbyBanksFromGoogle(String location) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                     location + "&radius=16093&type=bank&key=" + GOOGLE_API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.getForObject(url, Map.class);
        List<String> banks = new ArrayList<>();

        if (response != null && response.get("results") != null) {
            List results = (List) response.get("results");
            for (Object obj : results) {
                Map place = (Map) obj;
                String name = (String) place.get("name");
                String address = (String) place.get("vicinity");
                banks.add(name + " - " + address);
            }
        }

        return banks;
    }
}
