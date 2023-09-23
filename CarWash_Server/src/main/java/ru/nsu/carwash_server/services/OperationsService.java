package ru.nsu.carwash_server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.nsu.carwash_server.models.OperationsUserLink;
import ru.nsu.carwash_server.models.OperationsVersions;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.users.User;
import ru.nsu.carwash_server.payload.response.UserOperationsResponse;
import ru.nsu.carwash_server.repository.operations.OperationsRepository;
import ru.nsu.carwash_server.repository.operations.OperationsUsersLinkRepository;
import ru.nsu.carwash_server.repository.operations.OperationsVersionsRepository;
import ru.nsu.carwash_server.repository.orders.OrderVersionsRepository;
import ru.nsu.carwash_server.repository.orders.OrdersPolishingRepository;
import ru.nsu.carwash_server.repository.orders.OrdersRepository;
import ru.nsu.carwash_server.repository.orders.OrdersTireRepository;
import ru.nsu.carwash_server.repository.orders.OrdersWashingRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OperationsService {

    private final OperationsRepository operationsRepository;

    private final OperationsVersionsRepository operationsVersionsRepository;

    private final OperationsUsersLinkRepository operationsUsersLinkRepository;

    private final OrdersRepository ordersRepository;
    private final OrdersPolishingRepository ordersPolishingRepository;
    private final OrdersTireRepository ordersTireRepository;
    private final OrdersWashingRepository ordersWashingRepository;

    private final OrderVersionsRepository orderVersionsRepository;

    private final UserServiceImp userServiceImp;

    private EntityManager entityManager;

    @Autowired
    public OperationsService(OrdersRepository ordersRepository,
                             OperationsVersionsRepository operationsVersionsRepository,
                             OperationsRepository operationsRepository,
                             UserServiceImp userServiceImp,
                             OperationsUsersLinkRepository operationsUsersLinkRepository,
                             OrdersWashingRepository ordersWashingRepository,
                             OrdersTireRepository ordersTireRepository,
                             OrdersPolishingRepository ordersPolishingRepository,
                             OrderVersionsRepository orderVersionsRepository,
                             EntityManager entityManager) {
        this.entityManager = entityManager;
        this.ordersRepository = ordersRepository;
        this.userServiceImp = userServiceImp;
        this.operationsUsersLinkRepository = operationsUsersLinkRepository;
        this.operationsRepository = operationsRepository;
        this.operationsVersionsRepository = operationsVersionsRepository;
        this.ordersWashingRepository = ordersWashingRepository;
        this.orderVersionsRepository = orderVersionsRepository;
        this.ordersPolishingRepository = ordersPolishingRepository;
        this.ordersTireRepository = ordersTireRepository;
    }

    public void SaveUserOperation(String operationName, User user, String descriptionMessage, int version) {
        OperationsVersions operationsVersions = getOperationVersionByNameAndVersion(operationName, version);
        OperationsUserLink operationsUserLink = new OperationsUserLink();
        operationsUserLink.setDateOfCreation(new Date());
        operationsUserLink.setOperation(operationsVersions);
        operationsUserLink.setUser(user);

        operationsUserLink.setDescription(descriptionMessage);

        operationsUsersLinkRepository.save(operationsUserLink);
    }

    public OperationsVersions getLatestOperationsVersionById(Long id) {
        return operationsVersionsRepository.findLatestVersionByOperations_Id(id)
                .orElseThrow(() -> new NotInDataBaseException("услуг операций не найдена операция с айди: ",
                        id.toString()));
    }

    public List<OperationsVersions> getAllUserOperationsByIdOrUsername(Long id, String username) {
        if (id != null) {
            return operationsUsersLinkRepository.findAllOperationsByUserId(id);
        }
        Long userIdByName = userServiceImp.getActualUserVersionByUsername(username).getUser().getId();
        return operationsUsersLinkRepository.findAllOperationsByUserId(userIdByName);
    }

    public OperationsVersions getOperationVersionByIdAndVersion(Long id, Integer version) {
        return operationsVersionsRepository.findOperationsVersionsByVersionAndOperations_Id(version, id)
                .orElseThrow(() -> new NotInDataBaseException("услуг операций не найдена операция с айди: ",
                        id.toString() + " и версией:" + version.toString()));
    }

    public OperationsVersions getOperationVersionByNameAndVersion(String name, Integer version) {
        return operationsVersionsRepository.findByOperationNameAndVersion(name, version)
                .orElseThrow(() -> new NotInDataBaseException("услуг операций не найдена операция с именем: ",
                        name + " и версией:" + version));
    }

    public List<UserOperationsResponse> getRationalOperationForm(List<OperationsVersions> operationsVersions) {
        List<UserOperationsResponse> userOperationsResponses = new ArrayList<>();
        for (var singleOperation: operationsVersions) {
            UserOperationsResponse userOperationsResponse = new UserOperationsResponse();
            userOperationsResponse.setDateOfCreation(singleOperation.getDateOfCreation());
            userOperationsResponse.setVersion(singleOperation.getVersion());
            userOperationsResponse.setName(singleOperation.getOperations().getName());
            userOperationsResponse.setChanges(singleOperation.getChanges());
            userOperationsResponses.add(userOperationsResponse);
        }
        return userOperationsResponses;
    }
}
