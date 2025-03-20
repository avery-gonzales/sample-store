package org.example.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "link_click")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LinkClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_text_message_id")
    @JsonBackReference
    @ToString.Exclude
    private SentTextMessage sentTextMessage;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkClick linkClick = (LinkClick) o;
        return Objects.equals(id, linkClick.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 