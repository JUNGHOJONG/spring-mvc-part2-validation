package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    /**
     * Item 과 그 자식들을 모두 포용하고 싶을 때 사용(== 보다 효용성 높다)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Item item = (Item) target;

        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }

        Integer price = item.getPrice();

        if (price == null || (price < 1000 || price > 1000000)) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        Integer quantity = item.getQuantity();

        if (quantity == null || quantity > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 글로벌 검증
        if (price != null && quantity != null && price * quantity < 10000) {
            errors.reject("totalPriceMin", new Object[]{10000, (price * quantity)}, null);
        }
    }
}
