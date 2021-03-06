/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.boundary.serie;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.lpro.boundary.difficulty.DifficultyManager;
import org.lpro.boundary.game.GameManager;
import org.lpro.boundary.picture.PictureManager;
import org.lpro.entity.Difficulty;
import org.lpro.entity.Game;
import org.lpro.entity.Picture;
import org.lpro.entity.Serie;
import org.lpro.provider.Secured;


@Stateless
@Path("series")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Série")
public class SerieRessource {

    @Inject
    SerieManager sm;

    @Inject
    PictureManager pm;

    @Inject
    GameManager gm;

    @Inject
    DifficultyManager dm;

    @GET
    @ApiOperation(value = "Récupère toutes les séries", notes = "Renvoie le JSON associé à la collection de séries")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 417, message = "Expectation Failed"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response getSeries(@Context UriInfo uriInfo) {
        List<Serie> s = this.sm.findAll();
        List<Difficulty> d = this.dm.findAll();
        return Response.status(Response.Status.OK).entity(buildJsonSeries(s, d, uriInfo)).build();
    }
    
    @GET
    @Path("{id}")
    @ApiOperation(value = "Récupère une serie", notes = "Renvoie le JSON associé à une serie")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 417, message = "Expectation Failed"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response getSerie(@PathParam("id") String id, @Context UriInfo uriInfo) {
        Serie s = this.sm.findById(id);
        
        if(s == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Pas de série pour cette id")
                            .build()
            ).build();
        }
        
        return Response.status(Response.Status.OK).entity(buildJsonOneSerie(s, uriInfo)).build();
    }
    
    @GET
    @Path("{id}/games")
    @ApiOperation(value = "Récupère toutes les games d'une série", notes = "Renvoie le JSON associé à la collection de games d'une série")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 417, message = "Expectation Failed"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response getGamesSerie(@PathParam("id") String id, @Context UriInfo uriInfo) {
        Serie s = this.sm.findById(id);
        
        if(s == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Pas de série pour cette id")
                            .build()
            ).build();
        }
        
        List<Game> games = this.gm.findBySerieIdAndOrderByScore(s);
        return Response.status(Response.Status.OK).entity(buildJsonGames(s, games)).build();
    }

    @POST
    @Secured
    @ApiOperation(value = "Crée une série", notes = "Crée une série à partir du JSON fourni")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 417, message = "Expectation Failed"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response addSerie(JsonObject serieParam, @Context UriInfo uriInfo) throws java.text.ParseException {
        String uuid = UUID.randomUUID().toString();
        JsonObjectBuilder errors = Json.createObjectBuilder();
        JsonObject jsonSerie = null;
        JsonObject serieCoord = null;
        JsonArray pictures = null;
        Set<Picture> hspictures = new HashSet<Picture>();
        String errorsList = "";
        Boolean flag_errors = false;
        Boolean flag_errors_pictures = false;

        if(!serieParam.containsKey("serie") || serieParam.isNull("serie")){
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }else{
            jsonSerie = serieParam.getJsonObject("serie");
        }

        if(!jsonSerie.containsKey("name") || jsonSerie.isNull("name") || jsonSerie.getString("name").isEmpty()){
            errorsList += "Il faut renseigner un nom de série. ";
            flag_errors = true;
        }else if(!Pattern.matches("([a-zA-ZáàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\\s-]+)", jsonSerie.getString("name"))){
            errorsList += "Il faut respecter la casse du nom de série. ";
            flag_errors = true;
        }

        if(jsonSerie.containsKey("description") && Pattern.matches("([a-zA-ZáàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\\s-]+)", jsonSerie.getString("description"))){

        }else{
            errorsList += "Il faut respecter la casse de la description. ";
            flag_errors = true;
        }

        if(!jsonSerie.containsKey("city") || jsonSerie.isNull("city") || jsonSerie.getString("city").isEmpty()){
            errorsList += "Il faut renseigner une ville. ";
            flag_errors = true;
        }else if(!Pattern.matches("([a-zA-ZáàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ\\s-]+)", jsonSerie.getString("city"))){
            errorsList += "Il faut respecter la casse du nom de city. ";
            flag_errors = true;
        }

        if(!jsonSerie.containsKey("coords") || jsonSerie.isNull("coords")){
            errorsList += "Il faut renseigner les coordonnées d'une série. ";
            flag_errors = true;
        }else{
            serieCoord = jsonSerie.getJsonObject("coords");

            if(!serieCoord.containsKey("lat") || serieCoord.isNull("lat") || serieCoord.getString("lat").isEmpty()){
                errorsList += "Il faut renseigner la latitude de la série. ";
                flag_errors = true;
            }else if(!Pattern.matches("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,20})?))$", serieCoord.getString("lat"))){
                errorsList += "Il faut respecter la casse de la latitude de la ville. ";
                flag_errors = true;
            }

            if(!serieCoord.containsKey("lng") || serieCoord.isNull("lng") || serieCoord.getString("lng").isEmpty()){
                errorsList += "Il faut renseigner la longitude de la série. ";
                flag_errors = true;
            }else if(!Pattern.matches("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,20})?))$",serieCoord.getString("lng"))){
                errorsList += "Il faut respecter la casse de la longitude de la ville. ";
                flag_errors = true;
            }
        }

        if (flag_errors) {
            errors.add("errors", errorsList);
            JsonObject json_errors = errors.build();
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors).build();
        }


        if(!jsonSerie.containsKey("pictures") || jsonSerie.isNull("pictures")){
            errorsList += "Il faut renseigner les images d'une série. ";
            flag_errors_pictures = true;
        }else{
            pictures = jsonSerie.getJsonArray("pictures");
            System.out.println("taille: " + pictures.size());
            if(pictures.size() < 3){
                errorsList += "Il faut renseigner encore "+ (10-pictures.size())  +" images. ";
                flag_errors_pictures = true;
            }else{

                for(int i = 0; i < pictures.size(); i++){
                    if(!pictures.getJsonObject(i).containsKey("img") || pictures.getJsonObject(i).isNull("img") || pictures.getJsonObject(i).getString("img").isEmpty()){
                        errorsList += "Il faut renseigner un lien d'image pour l'image: " + (i+1) +". ";
                        flag_errors_pictures = true;
                    }

                    if(!pictures.getJsonObject(i).containsKey("coords") || pictures.getJsonObject(i).isNull("coords")){
                        errorsList += "Il faut renseigner les coordonnées de l'image: "+ (i+1) +". ";
                        flag_errors_pictures = true;
                    }else{
                        JsonObject serieCoordPictures = pictures.getJsonObject(i).getJsonObject("coords");

                        if(!serieCoordPictures.containsKey("lat") || serieCoordPictures.isNull("lat") || serieCoordPictures.getString("lat").isEmpty()){
                            errorsList += "Il faut renseigner la lattitude de l'image: "+ (i+1) +". ";
                            flag_errors_pictures = true;
                        }else if(!Pattern.matches("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,20})?))$",serieCoordPictures.getString("lat"))){
                            errorsList += "Il faut respecter la casse de la lattitude de l'image: "+ (i+1) + ". ";
                            flag_errors_pictures = true;
                        }

                        if(!serieCoordPictures.containsKey("lng") || serieCoordPictures.isNull("lng") || serieCoordPictures.getString("lng").isEmpty()){
                            errorsList += "Il faut renseigner la longitude de l'image: "+ (i+1) +". ";
                            flag_errors_pictures = true;
                        }else if(!Pattern.matches("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,20})?))$",serieCoordPictures.getString("lat"))){
                            errorsList += "Il faut respecter la casse de la longitude de l'image: "+ (i+1) + ". ";
                            flag_errors_pictures = true;
                        }

                        if(flag_errors_pictures){
                            errors.add("errors", errorsList);
                            JsonObject json_errors_pictures = errors.build();
                            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors_pictures).build();
                        }else{
                            String ext = pictures.getJsonObject(i).getString("img").substring(pictures.getJsonObject(i).getString("img").lastIndexOf("."), pictures.getJsonObject(i).getString("img").length());
                            String nom = uuid+"_"+Integer.valueOf(i+1).toString() + ext;
                            Picture pic = new Picture(uuid+"_"+pictures.getJsonObject(i).getString("img"), Double.parseDouble(serieCoordPictures.getString("lat")), Double.parseDouble(serieCoordPictures.getString("lng")));
                            pic = this.pm.save(pic);
                            hspictures.add(pic);
                        }
                    }
                }
            }
        }

        if(flag_errors_pictures){
            errors.add("errors", errorsList);
            JsonObject json_errors_pictures = errors.build();
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors_pictures).build();
        }

        Serie serie = new Serie(jsonSerie.getString("name"), jsonSerie.getString("description"), jsonSerie.getString("city"), Double.parseDouble(serieCoord.getString("lat")), Double.parseDouble(serieCoord.getString("lng")));
        serie.setId(uuid);
        Serie newSerie = this.sm.saveNewSeries(serie, hspictures);
        

        URI uri = uriInfo.getAbsolutePathBuilder().path("/"+newSerie.getId()).build();

        return Response.created(uri).entity(newSerie.getId()).build();
    }
    
    @PUT
    @Path("{id}")
    @Secured
    @ApiOperation(value = "Ajoute des images dans une série", notes = "Ajoute des images à une série à partir du JSON fourni")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 417, message = "Expectation Failed"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response addPictureToSerie(@PathParam("id") String id, JsonObject picturesObject, @Context UriInfo uriInfo) throws java.text.ParseException {
        
        Serie s = this.sm.findById(id);
        
        if(s == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Pas de série pour cette id")
                            .build()
            ).build();
        }
        
        JsonObjectBuilder errors = Json.createObjectBuilder();
        JsonArray pictures = null;
        Set<Picture> hspictures = new HashSet<Picture>();
        String errorsList = "";
        Boolean flag_errors_pictures = false;


        if(!picturesObject.containsKey("pictures") || picturesObject.isNull("pictures")){
            errorsList += "Il faut renseigner les images d'une série. ";
            flag_errors_pictures = true;
        }else{
            pictures = picturesObject.getJsonArray("pictures");
            System.out.println("taille: " + pictures.size());

                for(int i = 0; i < pictures.size(); i++){
                    if(!pictures.getJsonObject(i).containsKey("img") || pictures.getJsonObject(i).isNull("img") || pictures.getJsonObject(i).getString("img").isEmpty()){
                        errorsList += "Il faut renseigner un lien d'image pour l'image: " + (i+1) +". ";
                        flag_errors_pictures = true;
                    }

                    if(!pictures.getJsonObject(i).containsKey("coords") || pictures.getJsonObject(i).isNull("coords")){
                        errorsList += "Il faut renseigner les coordonnées de l'image: "+ (i+1) +". ";
                        flag_errors_pictures = true;
                    }else{
                        JsonObject serieCoordPictures = pictures.getJsonObject(i).getJsonObject("coords");

                        if(!serieCoordPictures.containsKey("lat") || serieCoordPictures.isNull("lat") || serieCoordPictures.getString("lat").isEmpty()){
                            errorsList += "Il faut renseigner la lattitude de l'image: "+ (i+1) +". ";
                            flag_errors_pictures = true;
                        }else if(!Pattern.matches("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,20})?))$",serieCoordPictures.getString("lat"))){
                            errorsList += "Il faut respecter la casse de la lattitude de l'image: "+ (i+1) + ". ";
                            flag_errors_pictures = true;
                        }

                        if(!serieCoordPictures.containsKey("lng") || serieCoordPictures.isNull("lng") || serieCoordPictures.getString("lng").isEmpty()){
                            errorsList += "Il faut renseigner la longitude de l'image: "+ (i+1) +". ";
                            flag_errors_pictures = true;
                        }else if(!Pattern.matches("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,20})?))$",serieCoordPictures.getString("lat"))){
                            errorsList += "Il faut respecter la casse de la longitude de l'image: "+ (i+1) + ". ";
                            flag_errors_pictures = true;
                        }

                        if(flag_errors_pictures){
                            errors.add("errors", errorsList);
                            JsonObject json_errors_pictures = errors.build();
                            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors_pictures).build();
                        }else{
                            String ext = pictures.getJsonObject(i).getString("img").substring(pictures.getJsonObject(i).getString("img").lastIndexOf("."), pictures.getJsonObject(i).getString("img").length());
                            String nom = id+"_"+Integer.valueOf(s.getPicture().size()+i+1).toString() + ext;
                            Picture pic = new Picture(id+"_"+pictures.getJsonObject(i).getString("img"), Double.parseDouble(serieCoordPictures.getString("lat")), Double.parseDouble(serieCoordPictures.getString("lng")));
                            pic = this.pm.save(pic);
                            hspictures.add(pic);
                        }
                    }
                
            }
        }

        if(flag_errors_pictures){
            errors.add("errors", errorsList);
            JsonObject json_errors_pictures = errors.build();
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors_pictures).build();
        }
        
        
       
        Serie newSerie = this.sm.saveNewPicturesSeries(s, hspictures);
        
        URI uri = uriInfo.getAbsolutePathBuilder().path("/"+newSerie.getId()).build();

        return Response.created(uri).entity(newSerie.getId()).build();
    }
    
    
    @POST
    @Path("{id}/upload")
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Importe une image", notes = "Importe une image et la sauveagrde sur le serveur")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response uploadFichier(@PathParam("id") String id, MultipartFormDataInput input) {

        this.pm.upload(input, id);
        return Response.status(200).entity(id).build();
    }
    
    @PUT
    @Path("{id}/upload")
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Importe une image", notes = "Importe une image et la sauveagrde sur le serveur")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response addMorePictures(@PathParam("id") String id, MultipartFormDataInput input) {

        this.pm.uploadToExistingSerie(input, id);
        return Response.status(200).entity(id).build();
    }
    
    @GET
    @Path("{id}/pictures/{picture}")
    @Produces("image/*")
    @ApiOperation(value = "Récupère une image", notes = "Récupère une image selon le nom passée dans l'url")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response getImage(@PathParam("id") String id, @PathParam("picture") String picture) {
      File f = new File("/opt/jboss/wildfly/standalone/tmp/img/" + id + "/"+ picture);
      if (!f.exists()) {
        throw new WebApplicationException(404);
      }
      String mt = new MimetypesFileTypeMap().getContentType(f);
      return Response.ok(f, mt).build();
    }
    
    private JsonObject buildJsonGames(Serie s, List<Game> g){
        JsonArrayBuilder games = Json.createArrayBuilder();

        g.forEach((game)->{
            JsonObject gam = Json.createObjectBuilder()
                    .add("id", game.getId())
                    .add("difficulty", this.dm.findById(game.getId_difficulty()).getLevel())
                    .add("nickname", game.getNickname())
                    .add("score", game.getScore())
                    .build();

            games.add(gam);
        });
        
        return Json.createObjectBuilder()
                .add("type", "collection")
                .add("games", games)
                .build();
    }
    
    private JsonObject buildJsonOneSerie(Serie serie, UriInfo uriInfo){
        JsonArrayBuilder picturesJA = Json.createArrayBuilder();

        serie.getPicture().forEach((picture ->{
            JsonObject coords = Json.createObjectBuilder()
                    .add("lat", picture.getLat())
                    .add("lng", picture.getLng())
                    .build();

            JsonObject pic = Json.createObjectBuilder()
                    .add("picture", uriInfo.getAbsolutePathBuilder().path("/pictures/"+ picture.getUrl()).build().toString())
                    .add("coords", coords)
                    .build();

            picturesJA.add(pic);
        }));
        
        return Json.createObjectBuilder()
                    .add("id", serie.getId())
                    .add("name", serie.getName())
                    .add("city", serie.getCity())
                    .add("description", serie.getDescription())
                    .add("lat", serie.getLat())
                    .add("lng", serie.getLng())
                    .add("pictures", picturesJA)
                    .build();
    }
    
    private JsonObject buildJsonSeries(List<Serie> s, List<Difficulty> d, UriInfo uriInfo){
        JsonArrayBuilder series = Json.createArrayBuilder();
        
        s.forEach((serie)->{
            JsonArrayBuilder picturesJA = Json.createArrayBuilder();
            serie.getPicture().forEach((picture ->{
                JsonObject coords = Json.createObjectBuilder()
                        .add("lat", picture.getLat())
                        .add("lng", picture.getLng())
                        .build();

                JsonObject pic = Json.createObjectBuilder()
                        .add("picture", uriInfo.getAbsolutePathBuilder().path("/"+serie.getId()+"/pictures/"+ picture.getUrl()).build().toString())
                        .add("coords", coords)
                        .build();

                picturesJA.add(pic);
            }));
             
            String img = (!serie.getPicture().isEmpty()) ? serie.getPicture().iterator().next().getUrl() : "";
            URI uri = uriInfo.getAbsolutePathBuilder().path("/"+serie.getId()+"/pictures/"+ img).build();
         
            JsonObject ser = Json.createObjectBuilder()
                    .add("id", serie.getId())
                    .add("name", serie.getName())
                    .add("city", serie.getCity())
                    .add("description", serie.getDescription())
                    .add("picture", uri.toString())
                    .add("picturesTab", picturesJA)
                    .build();

            series.add(ser);
        });

        JsonArrayBuilder difficulties = Json.createArrayBuilder();

        d.forEach((difficulty) -> {
            JsonArrayBuilder jsonDistances = Json.createArrayBuilder();
            JsonArrayBuilder jsonMultipliers = Json.createArrayBuilder();

            difficulty.getDistance().forEach((distance -> {
                jsonDistances.add(Json.createObjectBuilder()
                        .add("id_distance", distance.getId())
                        .add("distance", distance.getDistance())
                        .add("points", distance.getPoints())
                        .build());
            }));

            difficulty.getMultiplier().forEach((multiplier -> {
                jsonMultipliers.add(Json.createObjectBuilder()
                        .add("id_multiplier", multiplier.getId())
                        .add("multiplier", multiplier.getMultiplier())
                        .add("time", multiplier.getTime())
                        .build());
            }));

            JsonObject json = Json.createObjectBuilder()
                    .add("id", difficulty.getId())
                    .add("name", difficulty.getLevel())
                    .add("zoom", difficulty.getZoom())
                    .add("distances", jsonDistances.build())
                    .add("multipliers", jsonMultipliers.build())
                    .build();

            difficulties.add(json);
        });

        return Json.createObjectBuilder()
                .add("type", "collection")
                .add("difficulties", difficulties.build())
                .add("series", series.build())
                .build();
    }
}
