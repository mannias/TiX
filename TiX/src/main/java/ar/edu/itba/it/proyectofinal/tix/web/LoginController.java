package ar.edu.itba.it.proyectofinal.tix.web;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.it.proyectofinal.tix.domain.model.ISP;
import ar.edu.itba.it.proyectofinal.tix.domain.model.IspBoxplotDisplayer;
import ar.edu.itba.it.proyectofinal.tix.domain.model.IspHistogramDisplayer;
import ar.edu.itba.it.proyectofinal.tix.domain.model.Record;
import ar.edu.itba.it.proyectofinal.tix.domain.model.User;
import ar.edu.itba.it.proyectofinal.tix.domain.model.UserType;
import ar.edu.itba.it.proyectofinal.tix.domain.repository.RecordRepository;
import ar.edu.itba.it.proyectofinal.tix.domain.repository.UserRepository;
import ar.edu.itba.it.proyectofinal.tix.domain.util.AppError;
import ar.edu.itba.it.proyectofinal.tix.domain.util.ChartUtils;
import ar.edu.itba.it.proyectofinal.tix.web.command.forms.UserCreationForm;
import ar.edu.itba.it.proyectofinal.tix.web.command.forms.UserLoginForm;
import ar.edu.itba.it.proyectofinal.tix.web.util.ControllerUtil;

@Controller
public class LoginController {

	private UserRepository userRepo;
	private RecordRepository recordRepo;


	@Autowired
	public LoginController(UserRepository userRepo, RecordRepository recordRepo) {
		this.userRepo = userRepo;
		this.recordRepo = recordRepo;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView login(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		if (session.getAttribute("userId") != null) {
			mav.setView(ControllerUtil.redirectView("/user/dashboard"));
			return mav;
		}
		mav.setViewName("login/login");
		mav.addObject("userLoginForm", new UserLoginForm());
		mav.addObject("userCreationForm", new UserCreationForm());
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView login(HttpSession session, UserLoginForm userLoginForm, Errors errors) {
		ModelAndView mav = new ModelAndView();
		User user = userRepo.authenticate(userLoginForm.getNickname(), userLoginForm.getPassword());
		if (user != null) {
			session.setAttribute("userId", user.getId());
			if(user.getType().equals(UserType.ADMIN)){
				mav.setView(ControllerUtil.redirectView("/user/adminpanel"));
			}else{
				mav.setView(ControllerUtil.redirectView("/user/dashboard"));
			}
			return mav;
		}
		mav.addObject("userCreationForm", new UserCreationForm());
		userLoginForm.clearPassword();
		errors.rejectValue("nickname", AppError.LOGIN_FAILURE.translationKey);
		return mav;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	public void getcsv( 
			HttpSession session) {
		
		List<? extends Record> records = recordRepo.getAll();

		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream("tmp/records.cvs"), "utf-8"));
		    for(Record r: records){
		    	writer.write(r.toCSV()+ "\n");
		    }
		} catch (IOException ex) {
		  System.out.println("Error writing csv file");
		} finally {
		   try {writer.close();} 
		   catch (Exception ex) {}
		}
		System.out.println("CSV file successfully generated");
		
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView ispcharts( 
			HttpSession session) {
		ModelAndView mav = new ModelAndView();
				 

		List<ISP> isps = recordRepo.getISPs();

		List<IspHistogramDisplayer> disp_list = new ArrayList<IspHistogramDisplayer>();
		List<IspBoxplotDisplayer> boxplot_list = new ArrayList<IspBoxplotDisplayer>();
		
		//TODO 
		//Nowadays just shows the data from last 6 months
		 DateTime maxDate = new DateTime();
		 DateTime minDate = maxDate.minusDays(180);
		 
		for (ISP isp : isps) {
			int id = isp.getId();
			String name = isp.getName();
			List<Record> records = (List<Record>) recordRepo.getAllForIsp2(id, minDate,maxDate);
			
			System.out.println(minDate);
			System.out.println(maxDate);
			System.out.println("RECORDS: " + records);
			//histograms
			IspHistogramDisplayer disp = new IspHistogramDisplayer();
			disp.setIsp_id(id);		
			disp.setIsp_name(name);
			disp.setCongestionUpChart(ChartUtils.generateHistogramCongestionUp(records, "histograma congestion up"));
			disp.setCongestionDownChart(ChartUtils.generateHistogramCongestionDown(records, "histograma congestion down"));
			disp.setUtilizacionUpChart(ChartUtils.generateHistogramUtilizacionUp(records, "histograma utilizacion up"));
			disp.setUtilizacionDownChart(ChartUtils.generateHistogramUtilizacionDown(records, "histograma utilizacion down"));
			disp_list.add(disp);
			
			//boxplots
			IspBoxplotDisplayer boxplot = new IspBoxplotDisplayer();
			boxplot.setIsp_id((id));
			boxplot.setIsp_name(name);
			List<double[]> boxplotdata = new ArrayList<double[]>();
			boxplotdata = ChartUtils.generateBoxplot(records);
			if (!boxplotdata.isEmpty()){
				boxplot.setCongestionUpChart(boxplotdata.get(0));
				boxplot.setCongestionDownChart(boxplotdata.get(1));
				boxplot.setUtilizacionUpChart(boxplotdata.get(2));
				boxplot.setUtilizacionDownChart(boxplotdata.get(3));		
			}
			boxplot_list.add(boxplot);
			System.out.println(boxplot);
		}
		
		mav.addObject("disp_list", disp_list);
		mav.addObject("boxplot_list", boxplot_list);
		
		return mav;

	}

}
