package at.mctg.app.service.packages;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.Card;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Collection;
import java.util.List;


public class PackageController extends Controller {

    public PackageController() {}

    // POST /packages
    public Response createPackage(Request request) {
        return new Response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }
}
