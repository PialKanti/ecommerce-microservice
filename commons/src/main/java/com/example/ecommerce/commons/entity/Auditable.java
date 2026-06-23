package com.example.ecommerce.commons.entity;

import java.time.LocalDateTime;

public interface Auditable {
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getModifiedAt();
    void setModifiedAt(LocalDateTime modifiedAt);

    Long getCreatedBy();
    void setCreatedBy(Long createdBy);

    Long getModifiedBy();
    void setModifiedBy(Long modifiedBy);
}
