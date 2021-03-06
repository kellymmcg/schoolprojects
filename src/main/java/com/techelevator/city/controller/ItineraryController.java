package com.techelevator.city.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.city.model.City;
import com.techelevator.city.model.Itinerary;
import com.techelevator.city.model.ItineraryDAO;
import com.techelevator.city.model.Landmark;
import com.techelevator.city.model.LandmarkDAO;
import com.techelevator.city.model.ResourceNotFoundException;
import com.techelevator.city.model.UserDAO;

@Transactional
@Controller
@SessionAttributes("currentUser")
public class ItineraryController {

	private UserDAO userDAO;
	private LandmarkDAO landDAO;
	private ItineraryDAO itineraryDAO;
		
		@Autowired
		public ItineraryController(UserDAO userDAO, LandmarkDAO landDAO, ItineraryDAO itineraryDAO) {
			this.userDAO = userDAO;
			this.landDAO = landDAO;
			this.itineraryDAO = itineraryDAO;
		}
		
		@RequestMapping(path="/createItinerary", method=RequestMethod.GET)
		public String displayCreateItineraryPage() {
			return "createItinerary";
		}
		
		@RequestMapping(path="/manageItinerary", method=RequestMethod.GET)
		public String displayManageItineraryPage(@RequestParam Optional<Integer> id, ModelMap model) {
			if(! id.isPresent()){
				String userName = (String) model.get("currentUser");
				model.put("cItineraries", itineraryDAO.findCurrentItineraryByUser(userName));
				model.put("pItineraries", itineraryDAO.findCompletedItineraryByUser(userName));
				return "manageItinerary";
			}
			model.put("itinerary", itineraryDAO.findItineraryById(id.get(), (String)model.get("currentUser")));
			model.put("landmarks", landDAO.getLandmarksByItineraryId(id.get(), (String)model.get("currentUser")));
			if(model.get("itinerary") == null){
				throw new ResourceNotFoundException();
			}
			return "itineraryPage";
		}
		
		@ExceptionHandler(ResourceNotFoundException.class)
		@ResponseStatus(HttpStatus.NOT_FOUND)
		@RequestMapping(path="/notfound", method=RequestMethod.GET)
	    public String handleResourceNotFoundException() {
	        return "notfound";
	    }
	        
		
		
		@RequestMapping(path="/manageItinerary", method=RequestMethod.POST)
		public String deleteItinerary(@RequestParam String name, 
									  @RequestParam String userName, 
									  RedirectAttributes redir,
									  ModelMap model) {
			itineraryDAO.deleteItinerary(name, userName);
			redir.addFlashAttribute("notice", "You've succesfully deleted the itinerary!");
			return "redirect:/manageItinerary";
		}
		
		@RequestMapping(path="/addToItinerary", method=RequestMethod.POST)
		public String addLandmarkToItinerary(@RequestParam int id, 
											 @RequestParam int landmarkId, 
											 RedirectAttributes redir,
											 ModelMap model) {
			
			Itinerary itinerary = itineraryDAO.findItineraryById(id, (String)model.get("currentUser"));
			itinerary.setLandmark(landmarkId);
			itineraryDAO.addLandmarkToItinerary(itinerary);
			redir.addFlashAttribute("notice", "Your landmark was succesfully added!");
			return "redirect:/manageItinerary";
		}
		
		@RequestMapping (path="/completeItinerary", method=RequestMethod.POST)
		public String markItineraryAsComplete(@RequestParam int id, RedirectAttributes redir) {
			itineraryDAO.markItineraryAsCompleted(id);
			redir.addFlashAttribute("notice", "The itinerary was marked as 'Completed'.");
			return "redirect:/manageItinerary";
		}
		
		@RequestMapping (path="/incompleteItinerary", method=RequestMethod.POST)
		public String markItineraryAsIncompleted(@RequestParam int id, RedirectAttributes redir) {
			itineraryDAO.markItineraryAsIncompleted(id);
			redir.addFlashAttribute("notice", "The itinerary was marked as 'Incomplete'.");
			return "redirect:/manageItinerary";
		}
		
		@RequestMapping (path="/createNewItinerary", method=RequestMethod.POST)
		public String createNewItinerary(@RequestParam String name,
										 @RequestParam String description,
										 ModelMap model){
			
			itineraryDAO.createItinerary((String)model.get("currentUser"), name, description);
			model.put("notice", "You've successfully created a new Itinerary!");
			return "redirect:/manageItinerary";
		}
		
		@RequestMapping(path="generatedRoute", method=RequestMethod.GET)
		public String displayGeneratedRoute() {
			return "generatedRoute";
		}
		
		@RequestMapping(path="/itineraryPage", method=RequestMethod.POST)
		public String removeLandmarkFromItinerary(@RequestParam String user, @RequestParam int id, @RequestParam int iId, RedirectAttributes redir){
			itineraryDAO.removeLandmarkFromItinerary(id, user);
			redir.addFlashAttribute("notice", "You've removed the landmark from the itinerary!");
			return "redirect:/manageItinerary";
		}
		
		@ResponseBody
		@RequestMapping(path="/jsonRoute", method=RequestMethod.GET)
		public String generateJsonLandmarks(@RequestParam int id, ModelMap model) throws JsonProcessingException {

			ObjectMapper mapper = new ObjectMapper();
			
			List<Landmark> landmarks = landDAO.getLandmarksByItineraryId(id, (String)model.get("currentUser"));
			return mapper.writeValueAsString(landmarks);
		}
		
}
