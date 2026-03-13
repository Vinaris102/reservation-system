package com.vinaris.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }



    public Reservation getReservationById(Long id) {

        ReservationEntity reservationEntity =  repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id= " + id));


            return toDomainReservation(reservationEntity);
        }


    public List<Reservation> findAllReservation() {

        List<ReservationEntity> allEntities = repository.findAll();

        return allEntities.stream().map(this::toDomainReservation)
        .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty");
        }
        if (reservationToCreate.status() !=null){
            throw new IllegalArgumentException("Status should be empty");
        }
        var entityToSave = new ReservationEntity(
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING

        );
        var saveEntity = repository.save(entityToSave);

        return toDomainReservation(saveEntity);
}

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {

        var reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id= " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalArgumentException("Cannot modify reservation: status= " + reservationEntity.getStatus());
        }

        var reservationToSave = new ReservationEntity(
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING

        );
        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    public void deleteReservation(Long id) {
        if (!repository.existsById(id)){
            throw new NoSuchElementException("Not found reservation by id= " + id);
        }
        repository.deleteById(id);
    }


    public Reservation approveReservation(Long id) {

        var reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found reservation by id= " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(reservationEntity);

        if (isConflict){
            throw new IllegalStateException("Cannot approve reservation because of conflict: status= ");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflict(Reservation reservation){

        var allReservation = repository.findAll();
        for (ReservationEntity existingReservation: allReservation){
            if (reservation.getId().equals(existingReservation.getId())){
                continue;
            }
            if (!reservation.getRoomId().equals(existingReservation.getRoomId())){
                continue;
            }
            if (!existingReservation.getStatus().equals(ReservationStatus.APPROVED)){
                continue;
            }
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate())
                && existingReservation.getStartDate().isBefore(reservation.getEndDate())){
                    return true;
                }
            }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation){
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }
}
