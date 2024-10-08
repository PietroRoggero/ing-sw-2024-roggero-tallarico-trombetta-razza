package it.polimi.ingsw.model;

import java.io.Serializable;

/**
 * kind of card which is first placed by all the players at the center of the board, who can only decide whether to place it on the front or back side
 */
public class StarterCard extends Card implements Serializable {

    private final Resource secondResource;
    private final Resource thirdResource;

    /**
     * Default constructor for an empty starter card
     */
    public StarterCard() {
        secondResource = null;
        thirdResource = null;
        front = false;
        points = -1;
        resource = null;
        frontCorners = null;
        backCorners = null;
        id = "";
    }


    /**
     * Constructor of the class, initializes the resource cards with the given parameters
     * @param resource The type of resource of the card
     * @param secondResource The type of the potential second resource of the card
     * @param thirdResource The type of potential third resource of the card
     * @param frontCorners Corners of the front side of the card
     * @param backCorners Corners of the back side of the card
     * @param id String representing the id of the card, used for images
     */
     public StarterCard(Resource resource, Resource secondResource, Resource thirdResource, Corner[] frontCorners, Corner[] backCorners, String id) {
        this.secondResource = secondResource;
        this.thirdResource = thirdResource;
        this.resource = resource;
        for (int i = 0; i < frontCorners.length; i++) {
            this.frontCorners[i] = frontCorners[i];
        }
        for (int i = 0; i < backCorners.length; i++) {
            this.backCorners[i] = backCorners[i];
        }
        this.id = id;
    }



    @Override
    public Resource getResource() {
        return resource;
    }
    /**
     * Method that returns the card's potential second resource type
     * @return the card's resource type
     */
    public Resource getSecondResource() {
        return secondResource;
    }

    /**
     * Method that returns the card's potential third resource type
     * @return the card's resource type
     */
    public Resource getThirdResource() {
        return thirdResource;
    }
}
