package com.dwsoft.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import com.dwsoft.core.json.deser.MillisOrLocalDateTimeDeserializer;
import com.dwsoft.core.json.ser.StringOfLocalTimeSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Component
public class JacksonConfiguration implements Jackson2ObjectMapperBuilderCustomizer, Ordered {
	
	@Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String dateFormat;
	
	@Value("${spring.jackson.time-format:HH:mm:ss}")
    private String timeFormat;

	@Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        //所有日期类型的按TIMESTAMP序列化，LocalTime类型的按String序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateFormat)));
		// javaTimeModule.addDeserializer(LocalDateTime.class, new
		// LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateFormat)));
		javaTimeModule.addDeserializer(LocalDateTime.class, new MillisOrLocalDateTimeDeserializer());

        javaTimeModule.addSerializer(LocalTime.class, new StringOfLocalTimeSerializer(DateTimeFormatter.ofPattern(timeFormat)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(timeFormat)));
        
        jacksonObjectMapperBuilder.modules(javaTimeModule);
        
		jacksonObjectMapperBuilder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
