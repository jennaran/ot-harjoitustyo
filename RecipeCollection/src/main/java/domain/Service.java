
package domain;
    
import dao.RecipeDAO;
import dao.UserDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
/**
 * This class provides methods for managing users and recipes
 */
public class Service {
    
    private UserDAO userDao;
    private RecipeDAO recipeDAO;
    private User loggenInUser;

    public Service(UserDAO userDao, RecipeDAO recipeDAO) {
        this.userDao = userDao;
        this.recipeDAO = recipeDAO;
    }
    /**
    * This method is for creating a new recipe for the user who is logged in
    *
    * @param   name   recipe's name given by the user
    * @param   listIngredients   list of ingredients given by the user
    * @param   instructionWrong   instructions given by the user
    * 
    * @return true - if creating a recipe works
    */
    public boolean createNewRecipe(String name, List<String> listIngredients, String instructionWrong) {
        String recipeWithName = userRecipeNames().stream().filter(n -> n.equals(name)).findFirst().orElse(null);
        if (recipeWithName != null) {
            return false;
        }
        Recipe recipe = new Recipe(name, loggenInUser);
        String ingredients = ingredientsStringAddingLine(listIngredients);
        String instruction = instructionsStringAddingLine(instructionWrong);
        
        recipe.setIngredients(ingredients);
        recipe.setInstruction(instruction);
        try {
            recipeDAO.create(recipe);
        } catch (Exception e) {
            return false;
        }
        return true;
    } 
    /**
    * Modifies given list to a form where it can be saved to the recipe class
    *
    * @param   listIngredients   list of ingredients given by the user (or from createNewRecipe)
    * 
    * @return Ingredients as String
    */
    public String ingredientsStringAddingLine(List<String> listIngredients) {
        String ingredients = "";
        ingredients = listIngredients.stream().map((ingredient) -> ingredient + "_").reduce(ingredients, String::concat);
	return ingredients.substring(0, ingredients.length() - 1);
    }
    /**
    * Modifies given String to a form where it can be saved to the recipe class
    *
    * @param   wrong   instructions given by the user (or from createNewRecipe)
    * 
    * @return Instructions as String
    */
    public String instructionsStringAddingLine(String wrong) {
        String instructions = "";
        instructions = wrong.replace("\n", "_");
        return instructions;
    }
    /**
    * This method is for finding a specific recipe from the user who is logged in
    *
    * @param   name   recipe's name given by the user 
    * 
    * @return The recipe with the given name
    */
    public Recipe findUsersRecipeByName(String name) {
        List<Recipe> recipes = this.recipeDAO.listUsersAll(loggenInUser);
        Recipe recipe = recipes.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
        return recipe;
    }
    /**
    * This method is for finding the ingredients 
    * of the given user's recipe
    *
    * @param   name   recipe's name given by the user
    * 
    * @return List of the ingredients
    */
    public List<String> getRecipeIngredienstByRecipeName(String name) {
        Recipe recipe = findUsersRecipeByName(name); 
        if (recipe == null) {
            return null;
        }
        List<String> ingredientsList = recipe.getIngredientsList();
        return ingredientsList;
    }
    /**
    * This method is for finding the instructions 
    * of the given user's recipe
    *
    * @param   name   recipe's name given by the user
    * 
    * @return Instructions as String without "_"
    */
    public String getRecipeInstructionsByRecipeName(String name) {
        Recipe recipe = findUsersRecipeByName(name);
        if (recipe == null) {
            return null;
        }
        String Instructions = recipe.getInstruction();
        Instructions = Instructions.replace("_", "\n");
        return Instructions;
    }
    /**
    * This method is for creating a new user with a unique username
    *
    * @param   username   username of the new user
    * @param   password   password of the new user
    * 
    * @return true - if creating a user works
    */
    public boolean createNewUser(String username, String password) {
        if (userDao.searchByUsername(username) != null) {
            return false;
        }
        User user = new User(username, password);
        try {
            userDao.create(user);
        } catch (Exception e) {
            return false;
        }
        return true;
    } 
    /**
    * This method is for login.
    * It sets a value for the loggedInUser
    *
    * @param   username   user's username
    * @param   password   user's password
    * 
    * @return true - if login works (=user exists)
    */
    public boolean logIn(String username, String password) {
        User user = userDao.searchByUsername(username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                this.loggenInUser = user;
                return true;
            }
        } 
        return false;
    }
    
    public User getLoggenInUser() {
        return loggenInUser;
    }
     
    public void logOut() {
        this.loggenInUser = null;
    }
    /**
    * This method is for deleting the loggedIn user and all their recipes
    *
    * @return true - if deleting user and recipes works
    */
    public boolean deleteAccount() {
        try {
            userDao.delete(this.loggenInUser);
            this.recipeDAO.delete(loggenInUser);
        } catch (Exception ex) {
            return false;
        }
        logOut();
        return true;
    }
    /**
    * This method is for listing the names of the loggedInUser's recipes
    *
    * @return List of the recipe names
    */
    public List<String> userRecipeNames() {
        List<Recipe> recipes = this.recipeDAO.listUsersAll(loggenInUser);
        if (recipes == null) {
            List<String> recipeNames = new ArrayList();
            return recipeNames;
        }
        
        List<String> recipeNames = recipes.stream().map(r -> r.getName()).collect(Collectors.toList());
        return recipeNames;
    }

}
