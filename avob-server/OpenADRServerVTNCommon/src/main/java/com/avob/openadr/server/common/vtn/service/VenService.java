package com.avob.openadr.server.common.vtn.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.avob.openadr.server.common.vtn.exception.GenerateX509VenException;
import com.avob.openadr.server.common.vtn.models.Target;
import com.avob.openadr.server.common.vtn.models.TargetTypeEnum;
import com.avob.openadr.server.common.vtn.models.demandresponseevent.DemandResponseEvent;
import com.avob.openadr.server.common.vtn.models.ven.Ven;
import com.avob.openadr.server.common.vtn.models.ven.VenCreateDto;
import com.avob.openadr.server.common.vtn.models.ven.VenDao;
import com.avob.openadr.server.common.vtn.models.ven.VenSpecification;
import com.avob.openadr.server.common.vtn.models.ven.filter.VenFilter;
import com.avob.openadr.server.common.vtn.models.vencredential.VenCredential;
import com.avob.openadr.server.common.vtn.models.vencredential.VenCredentialDao;
import com.avob.openadr.server.common.vtn.models.vendemandresponseevent.VenDemandResponseEvent;
import com.avob.openadr.server.common.vtn.models.vendemandresponseevent.VenDemandResponseEventDao;
import com.avob.openadr.server.common.vtn.models.vengroup.VenGroup;
import com.avob.openadr.server.common.vtn.models.vengroup.VenGroupDao;
import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContext;
import com.avob.openadr.server.common.vtn.models.venmarketcontext.VenMarketContextDao;
import com.avob.openadr.server.common.vtn.models.venresource.VenResourceDao;
import com.avob.openadr.server.common.vtn.security.DigestAuthenticationProvider;
import com.avob.openadr.server.common.vtn.service.dtomapper.VenDtoMapper;

@Service
public class VenService extends AbstractUserService<Ven> {

	private static final Integer DEFAULT_SEARCH_SIZE = 20;

	@Resource
	private VenDao venDao;

	@Resource
	private VenResourceDao venResourceDao;

	@Resource
	private VenCredentialDao venCredentialDao;

	/**
	 * VEN 을 지우기 전에 불릴 모듈별 뒷정리. 없으면 비어 있다.
	 * VenDeleteHandler 의 주석에 이유를 적어 뒀다.
	 */
	@Autowired(required = false)
	private List<VenDeleteHandler> venDeleteHandlers = new ArrayList<>();

	@Resource
	private VenDemandResponseEventDao venDemandResponseEventDao;
	
	@Resource
	private VenGroupDao venGroupDao;
	
	@Resource
	private VenMarketContextDao venMarketContextDao;

	@Autowired(required = false)
	private GenerateX509CertificateService generateX509VenService;

	@Resource
	private DigestAuthenticationProvider digestAuthenticationProvider;

	@Resource
	private VenDtoMapper venDtoMapper;

	public Ven prepare(String username, String password) {
		return super.prepare(new Ven(), username, password, digestAuthenticationProvider.getRealm());
	}

	/**
	 * @param username
	 * @return
	 */
	public Ven prepare(String username) {
		return super.prepare(new Ven(), username);
	}

	/**
	 * @param username
	 * @return
	 */
	public Ven prepare(VenCreateDto dto) {
		Ven prepare;
		if (dto.getPassword() != null) {
			prepare = super.prepare(new Ven(), dto.getUsername(), dto.getPassword(),
					digestAuthenticationProvider.getRealm());
		} else {
			prepare = super.prepare(new Ven(), dto.getUsername());
		}

		venDtoMapper.copyToVen(dto, prepare);

		return prepare;
	}

	public Optional<File> generateCertificateIfRequired(VenCreateDto dto, Ven ven) throws GenerateX509VenException {

		if (dto.getAuthenticationType() != null && !"no".equals(dto.getAuthenticationType())
				&& dto.getNeedCertificateGeneration() != null) {

			if (generateX509VenService != null) {
				File generateCredentials = generateX509VenService.generateCredentials(dto, ven);
				return Optional.of(generateCredentials);
			} else {
				throw new GenerateX509VenException(
						"Client certificate feature require CA certificate to be provided to the vtn");
			}
		}

		return Optional.empty();

	}

	@Override
	@Transactional
	public void delete(Ven instance) {

		// 다른 모듈이 들고 있는 행을 먼저 치운다.
		// 이게 없으면 VTN20b 의 리포트 테이블에 걸려서 삭제가 외래키 위반으로 터진다
		venDeleteHandlers.forEach(handler -> handler.onVenDelete(instance));

		venResourceDao.deleteByVenId(instance.getId());
		venDemandResponseEventDao.deleteByVenId(instance.getId());
		venCredentialDao.deleteByVenId(instance.getId());
		venDao.delete(instance);
	}

	@Override
	public void delete(Iterable<Ven> instances) {
		instances.forEach(ven -> { this.delete(ven);});
	}

	@Override
	public Ven save(Ven instance) {
		return venDao.save(instance);
	}

	@Override
	public void save(Iterable<Ven> instances) {
		venDao.saveAll(instances);
	}

	public Ven findOneByUsername(String username) {
		return venDao.findOneByUsername(username);
	}

	public Ven findOneByRegistrationId(String registrationId) {
		return venDao.findOneByRegistrationId(registrationId);
	}

	public List<Ven> findByUsernameInAndVenMarketContextsContains(List<String> username,
			VenMarketContext venMarketContext) {
		return venDao.findByUsernameInAndVenMarketContextsContains(username, venMarketContext);
	}

	public List<Ven> findByGroupName(List<String> groupName) {
		return venDao.findByVenGroupsName(groupName);
	}

	public List<Ven> findByMarketContextName(List<String> groupName) {
		return venDao.findByVenMarketContextsName(groupName);
	}

	public Ven findOne(Long id) {
		return venDao.findById(id).get();
	}

	public Iterable<Ven> findAll() {
		return venDao.findAll();
	}

	public long count() {
		return venDao.count();
	}

	public void cleanRegistration(Ven ven) {
		ven.setRegistrationId(null);
		this.save(ven);
	}

	/**
	 * VTN 이 만든 VEN 인증서 묶음(tar)을 남긴다. 이미 있으면 바꾼다.
	 * VEN 을 만들 때와 다시 만들 때(regenerateCredentials) 부른다. ven 은 저장된 뒤라 id 가 있어야 한다
	 */
	public void saveCredentials(Ven ven, File credentials) throws GenerateX509VenException {
		byte[] data;
		try {
			data = Files.readAllBytes(credentials.toPath());
		} catch (IOException e) {
			throw new GenerateX509VenException(e);
		}
		VenCredential credential = venCredentialDao.findOneByVenId(ven.getId());
		if (credential == null) {
			credential = new VenCredential();
			credential.setVenId(ven.getId());
		}
		credential.setFileName(ven.getCommonName() + "-credentials.tar");
		credential.setData(data);
		credential.setCreatedDatetime(System.currentTimeMillis());
		venCredentialDao.save(credential);
	}

	/** 남겨 둔 인증서 묶음. VTN 이 인증서를 만들지 않은 VEN 이나 이 기능 전에 만든 VEN 은 없다 */
	public Optional<VenCredential> findCredentials(Ven ven) {
		return Optional.ofNullable(venCredentialDao.findOneByVenId(ven.getId()));
	}

	public boolean hasCredentials(Ven ven) {
		return venCredentialDao.existsByVenId(ven.getId());
	}

	/**
	 * VEN 인증서를 새 키로 다시 만들고 남긴다.
	 *
	 * VenID 는 인증서 지문이라 새 지문으로 바뀐다(generateCredentials 가 username 을 바꾼다).
	 * 예전 인증서로 한 등록은 쓸 수 없으니 registrationId 도 지운다. VEN 은 새 인증서로 다시 등록해야 한다.
	 * 이벤트, 가입(MarketContext), 그룹, 리소스는 VEN 행(id)에 붙어 있어서 그대로 남는다
	 *
	 * @param algorithm rsa 또는 ecc
	 */
	@Transactional
	public File regenerateCredentials(Ven ven, String algorithm) throws GenerateX509VenException {
		if (generateX509VenService == null) {
			throw new GenerateX509VenException(
					"Client certificate feature require CA certificate to be provided to the vtn");
		}
		if (ven.getCommonName() == null || ven.getCommonName().isBlank()) {
			throw new GenerateX509VenException("Ven has no common name: " + ven.getUsername());
		}
		VenCreateDto dto = new VenCreateDto();
		dto.setCommonName(ven.getCommonName());
		dto.setAuthenticationType("x509");
		dto.setNeedCertificateGeneration(algorithm);
		File credentials = generateX509VenService.generateCredentials(dto, ven);
		ven.setRegistrationId(null);
		Ven saved = this.save(ven);
		saveCredentials(saved, credentials);
		return credentials;
	}

	public Page<Ven> search(List<VenFilter> filters) {
		return search(filters, null, null);
	}

	public Page<Ven> search(List<VenFilter> filters, Integer page, Integer size) {
		if (page == null) {
			page = 0;
		}
		if (size == null) {
			size = DEFAULT_SEARCH_SIZE;
		}
		Sort sort = Sort.by(Sort.Order.desc("registrationId"), Sort.Order.asc("commonName"));
		PageRequest of = PageRequest.of(page, size, sort);
		return venDao.findAll(VenSpecification.search(filters), of);
	}
	
	
	public void addVenDemandResponseEvent(Ven ven, DemandResponseEvent event) {
		VenDemandResponseEvent el = venDemandResponseEventDao
				.findOneByEventIdAndVenUsername(event.getId(), ven.getUsername());
		
		if(el == null) {
			el = new VenDemandResponseEvent(event, ven);
		}
		venDemandResponseEventDao.save(el);
	}
	
	@Transactional
	public boolean isVenTargetedBy(Ven ven, DemandResponseEvent event) {
		
		boolean targeted = false;
		for(Target target : event.getTargets()) {
			if(TargetTypeEnum.VEN.equals(target.getTargetType())) {
				
				if( ven.getUsername().equals(target.getTargetId())) {
					targeted = true;
				}
				
			} else if(TargetTypeEnum.GROUP.equals(target.getTargetType())) {
				
				
				Set<VenGroup> venGroups = ven.getVenGroups();
				if(venGroups != null) {
					for(VenGroup group : venGroups) {
						if(group.getName().equals(target.getTargetId())) {
							targeted = true;
						}
					}
				}
				
				
			} 
			
			if(event.getDescriptor().getMarketContext() != null) {

				
				Set<VenMarketContext> venMarketContext =  ven.getVenMarketContexts();
				if(venMarketContext != null) {
					for(VenMarketContext marketContext : venMarketContext) {
						if(marketContext.getName().equals(event.getDescriptor().getMarketContext().getName())) {
							targeted = true;
						}
					}
				}
			}
			
		}

		return targeted;
	}

}
