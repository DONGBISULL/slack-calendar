package com.demo.slackcalendar.app.slack.repository;

import com.demo.slackcalendar.app.slack.entity.SlackUser;
import com.slack.api.Slack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlackUserRepository extends JpaRepository<SlackUser, Long> {

    Optional<SlackUser> findByTeamIdAndUserId(String teamId, String userId);

}
