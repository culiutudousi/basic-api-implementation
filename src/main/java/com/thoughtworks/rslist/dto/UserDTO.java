package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotNull
    @Size(max = 8)
    private String name;
    @NotNull
    private String gender;
    @NotNull
    @Min(18)
    @Max(100)
    private Integer age;
    @Email
    private String email;
    @Pattern(regexp = "1\\d{10}")
    private String phone;
}
