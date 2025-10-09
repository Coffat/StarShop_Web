package com.example.demo.controller;

import com.example.demo.dto.location.DistrictDto;
import com.example.demo.dto.location.ProvinceDto;
import com.example.demo.dto.location.WardDto;
import com.example.demo.service.LocationService;
import com.example.demo.dto.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    
    private final LocationService locationService;
    
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }
    
    /**
     * Get all provinces
     */
    @GetMapping("/provinces")
    public ResponseEntity<ResponseWrapper<List<ProvinceDto>>> getProvinces() {
        try {
            logger.info("Getting provinces from LocationService");
            List<ProvinceDto> provinces = locationService.getProvinces();
            logger.info("Retrieved {} provinces", provinces.size());
            
            if (provinces.isEmpty()) {
                return ResponseEntity.ok(ResponseWrapper.error("Không thể tải danh sách tỉnh/thành phố"));
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(provinces));
            
        } catch (Exception e) {
            logger.error("Error getting provinces", e);
            return ResponseEntity.ok(ResponseWrapper.error("Lỗi hệ thống khi tải danh sách tỉnh/thành phố"));
        }
    }
    
    /**
     * Debug endpoint to check GHN configuration
     */
    @GetMapping("/debug")
    public ResponseEntity<ResponseWrapper<String>> debug() {
        try {
            boolean isValid = locationService.isGhnConfigurationValid();
            String message = "GHN Configuration is " + (isValid ? "VALID" : "INVALID");
            return ResponseEntity.ok(ResponseWrapper.success(message));
        } catch (Exception e) {
            logger.error("Error checking GHN configuration", e);
            return ResponseEntity.ok(ResponseWrapper.error("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get districts by province ID
     */
    @GetMapping("/districts")
    public ResponseEntity<ResponseWrapper<List<DistrictDto>>> getDistricts(@RequestParam int province_id) {
        try {
            List<DistrictDto> districts = locationService.getDistricts(province_id);
            
            if (districts.isEmpty()) {
                return ResponseEntity.ok(ResponseWrapper.error("Không thể tải danh sách quận/huyện cho tỉnh/thành phố này"));
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(districts));
            
        } catch (Exception e) {
            logger.error("Error getting districts for province {}", province_id, e);
            return ResponseEntity.ok(ResponseWrapper.error("Lỗi hệ thống khi tải danh sách quận/huyện"));
        }
    }
    
    /**
     * Get wards by district ID
     */
    @GetMapping("/wards")
    public ResponseEntity<ResponseWrapper<List<WardDto>>> getWards(@RequestParam int district_id) {
        try {
            List<WardDto> wards = locationService.getWards(district_id);
            
            if (wards.isEmpty()) {
                return ResponseEntity.ok(ResponseWrapper.error("Không thể tải danh sách phường/xã cho quận/huyện này"));
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(wards));
            
        } catch (Exception e) {
            logger.error("Error getting wards for district {}", district_id, e);
            return ResponseEntity.ok(ResponseWrapper.error("Lỗi hệ thống khi tải danh sách phường/xã"));
        }
    }
    
    /**
     * Get all wards by province ID (for 2-level address mode)
     */
    @GetMapping("/wards-by-province")
    public ResponseEntity<ResponseWrapper<List<WardDto>>> getWardsByProvince(@RequestParam int province_id) {
        try {
            logger.info("Getting wards for province: {}", province_id);
            List<WardDto> wards = locationService.getWardsByProvince(province_id);
            
            if (wards.isEmpty()) {
                return ResponseEntity.ok(ResponseWrapper.error("Không thể tải danh sách phường/xã cho tỉnh/thành phố này"));
            }
            
            logger.info("Retrieved {} wards for province {}", wards.size(), province_id);
            return ResponseEntity.ok(ResponseWrapper.success(wards));
            
        } catch (Exception e) {
            logger.error("Error getting wards for province {}", province_id, e);
            return ResponseEntity.ok(ResponseWrapper.error("Lỗi hệ thống khi tải danh sách phường/xã"));
        }
    }
    
    /**
     * Check if GHN service is available
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseWrapper<Boolean>> getServiceStatus() {
        try {
            boolean isAvailable = locationService.isGhnServiceAvailable();
            return ResponseEntity.ok(ResponseWrapper.success(isAvailable));
            
        } catch (Exception e) {
            logger.error("Error checking GHN service status", e);
            return ResponseEntity.ok(ResponseWrapper.error("Lỗi hệ thống khi kiểm tra trạng thái dịch vụ"));
        }
    }
}
