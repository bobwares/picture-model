/**
 * App: Picture Model
 * Package: com.picturemodel.api.dto.request
 * File: TagUpdateRequest.java
 * Version: 0.1.0
 * Turns: 7
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-31T08:41:53Z
 * Exports: TagUpdateRequest
 * Description: DTO for updating tags. Methods: getName - get name; getColor - get color.
 */

package com.picturemodel.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for updating a tag.
 */
@Data
public class TagUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 7)
    private String color;
}
