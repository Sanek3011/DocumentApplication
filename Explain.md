```sql
SELECT *
FROM documents
WHERE status = 'SUBMITTED'
ORDER BY created_at
LIMIT 20;
```
```sql
Limit  (cost=8.17..8.17 rows=1 width=1588) (actual time=0.036..0.038 rows=6 loops=1)
  ->  Sort  (cost=8.17..8.17 rows=1 width=1588) (actual time=0.035..0.036 rows=6 loops=1)
        Sort Key: created_at
        Sort Method: quicksort  Memory: 25kB
        ->  Index Scan using idx_documents_status on documents  (cost=0.14..8.16 rows=1 width=1588) (actual time=0.025..0.028 rows=6 loops=1)
              Index Cond: ((status)::text = 'APPROVED'::text)
Planning Time: 0.120 ms
Execution Time: 0.057 ms
```
# Индексы
## documents
- индекс по status — для удобства выборки по статусам, чтобы избежать полного сканирования таблицы  
- индекс по created_at — так как поле неизменяемое, позволяет эффективно выбирать последние документы  

## documents_history
- document_id — типичный сценарий загрузки истории по документу, избегаем полного сканирования таблицы  
- actioned_at — неизменяемая дата, позволяет быстро получать события за определённый промежуток времени  

## registry
- document_id — быстрая проверка записи в реестре для конкретного документа, также записи не изменяются  
- approved_at — неизменяемая дата, быстрая выборка по дате  
