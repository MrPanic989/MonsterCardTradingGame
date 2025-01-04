package at.mctg.app.service.stats;

import at.mctg.app.controller.Controller;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.StatsDTO;
import at.mctg.app.model.User;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class StatsController extends Controller {
    public StatsController() {
        super();
    }
    //GET /stats
    public Response getUserStats(Request request) {
        //Checking the tooken
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\":\"Invalid token\" }"
            );
        }
        String token = authHeader.substring("Bearer ".length());

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {

            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"No user for token\" }");
            }

            unitOfWork.commitTransaction();

            StatsDTO userStats = new StatsDTO(
                    user.getName(),
                    user.getElo(),
                    user.getWins(),
                    user.getLosses()
            );

            String statsJSON = this.getObjectMapper().writeValueAsString(userStats);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    statsJSON);

        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\":\"Internal Server Error\" }"
            );
        }
    }

    //GET /scoreboard
    //Retrives a list of Stats ordered by the ELO
    public Response getScoreboard(Request request) {

        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\":\"Invalid token\" }"
            );
        }
        String token = authHeader.substring("Bearer ".length());

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User currentUser = new UserRepository(unitOfWork).findByAuthToken(token);
            if (currentUser == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"No user for token\" }"
                );
            }

            // Load all users
            Collection<User> allUsers = new UserRepository(unitOfWork).findAllUsers();
            // Sort by ELO descending
            List<User> userList = new ArrayList<>(allUsers);
            userList.sort(Comparator.comparingInt(User::getElo).reversed());

            if (userList.isEmpty()) {
                return new Response(
                        HttpStatus.NO_CONTENT,
                        ContentType.JSON,
                        ""
                );
            }

            // Building a JSON array
            // e.g. { "Name": "Kienboeck", "Elo": 123, "Wins": 10, "Losses": 5 } , { ... }
            List<StatsDTO> statsDTOList = new ArrayList<>(userList.size());
            for (User user : userList) {
                StatsDTO statsDTO = new StatsDTO(
                        user.getName(),
                        user.getElo(),
                        user.getWins(),
                        user.getLosses()
                );
                statsDTOList.add(statsDTO);
            }
            unitOfWork.commitTransaction();

            String statsJSON = this.getObjectMapper().writeValueAsString(statsDTOList);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    statsJSON
            );

        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\":\"Internal Server Error\" }"
            );
        }
    }
}
