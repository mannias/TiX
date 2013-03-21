package ar.edu.itba.it.proyectofinal.internetqos.web.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ar.edu.itba.it.proyectofinal.internetqos.domain.model.User;
import ar.edu.itba.it.proyectofinal.internetqos.domain.repository.UserRepository;

@Component
public class IdToUserConverter implements Converter<Integer, User> {
	
	private UserRepository userRepository;
	
	@Autowired
	public IdToUserConverter(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public User convert(Integer userId) {
		return userRepository.get(userId);
	}
	
	

}
