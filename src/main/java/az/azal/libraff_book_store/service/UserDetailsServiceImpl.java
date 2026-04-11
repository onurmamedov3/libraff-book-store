package az.azal.libraff_book_store.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String fin) throws UsernameNotFoundException {
		// 1. Search by FIN
		EmployeeEntity employee = employeeRepository.findByFIN(fin)
				.orElseThrow(() -> new UsernameNotFoundException("Employee not found with FIN: " + fin));

		Set<GrantedAuthority> authorities = employee.getPosition().getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());

		// 2. Map it to Spring Security's User object.
		// We pass the FIN into the first parameter (which Spring calls 'username')
		return new User(employee.getFIN(), employee.getPassword(), employee.getIsActive(), // enabled
				true, // accountNonExpired
				true, // credentialsNonExpired
				true, // accountNonLocked
				authorities);
	}

}