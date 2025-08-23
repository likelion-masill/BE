package project.masil.community.event;

public record EventCreatedEvent(
    Long postId,
    Long regionId,
    String title,
    String content
) {

}