package yuan.expand.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yuan.core.list.BaseViewHolder;
import yuan.expand.R;


/**
 * Created by YuanYe on 2018/1/17.
 * 使用RecyclerView实现折叠展开（带默认展开、折叠动画）
 * 使用时，只需继承ExpandableAdapter重写方法即可
 */
public abstract class ExpandableAdapter<S extends ExpandableSection, I extends ExpandableItem> extends RecyclerView.Adapter<BaseViewHolder> {

    protected Context mContext;

    private static final int SECTION_TYPE = 1;
    private static final int ITEM_TYPE = 2;

    protected List<Object> mData;

    public ExpandableAdapter(Context context) {
        this.mContext = context;
        mData = new ArrayList<>();
        //初始化数据
        for (int i = 0; i < getGroupData().size(); i++) {
            getGroupData().get(i).setGroupPosition(i);
            mData.add(getGroupData().get(i));
            for (int j = 0; j < getChildData(i).size(); j++) {
                getChildData(i).get(j).setGroupPosition(i);
                getChildData(i).get(j).setChildPosition(j);
                if (getGroupData().get(i).isExpandable()) {
                    mData.add(getChildData(i).get(j));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof ExpandableSection) {
            return SECTION_TYPE;
        } else if (mData.get(position) instanceof ExpandableItem) {
            return ITEM_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE) {
            int layout = getChildLayout(parent, viewType);
            return new ChildHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
        } else if (viewType == SECTION_TYPE) {
            int layout = getGroupLayout(parent, viewType);
            return new GroupHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
        }
        return onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        holder.itemView.setTag(R.id.item_position, position);
        switch (getItemViewType(position)) {
            case SECTION_TYPE:
                final S section = (S) mData.get(position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onGroupItemClick((GroupHolder) holder, section.getGroupPosition(), section.isExpandable());
                        if (section.isExpandable()) {
                            closeList(section.getGroupPosition());
                            section.setExpandable(false);
                        } else {
                            openList(section.getGroupPosition());
                            section.setExpandable(true);
                        }
                    }
                });
                onBindGroupHolder((GroupHolder) holder, section.getGroupPosition());
                break;
            case ITEM_TYPE:
                final I item = (I) mData.get(position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onChildItemClick((ChildHolder) holder, item.getGroupPosition(), item.getChildPosition());
                    }
                });
                onBindChildHolder((ChildHolder) holder, item.getGroupPosition(), item.getChildPosition());
                break;
        }
    }

    /**
     * 点击关闭时的方法
     */
    private void closeList(int groupPosition) {
        //计算当前插入的位置
        int addPosition = 1;
        for (int i = 0; i < groupPosition; i++) {
            addPosition++;
            if (getGroupData().get(i).isExpandable()) { //是否展开
                for (int j = 0; j < getChildData(i).size(); j++) {
                    addPosition++;
                }
            }
        }
        for (int i = 0; i < getChildData(groupPosition).size(); i++) {
            mData.remove(getChildData(groupPosition).get(i));
        }
        notifyItemRangeRemoved(addPosition, getChildData(groupPosition).size());
    }

    /**
     * 点击打开时的方法
     */
    private void openList(int groupPosition) {
        //计算当前插入的位置
        int addPosition = 0;
        for (int i = 0; i <= groupPosition; i++) {
            addPosition++;
            if (getGroupData().get(i).isExpandable()) { //是否展开
                for (int j = 0; j < getChildData(i).size(); j++) {
                    addPosition++;
                }
            }
        }
        for (int i = 0; i < getChildData(groupPosition).size(); i++) {
            mData.add(addPosition + i, getChildData(groupPosition).get(i));
        }
        notifyItemRangeInserted(addPosition, getChildData(groupPosition).size());
    }

    public abstract void onGroupItemClick(GroupHolder holder, int groupPosition, boolean isExpandable);

    public abstract void onChildItemClick(ChildHolder holder, int groupPosition, int childPosition);

    public abstract
    @LayoutRes
    int getGroupLayout(ViewGroup parent, int viewType);

    public abstract
    @LayoutRes
    int getChildLayout(ViewGroup parent, int viewType);

    public abstract List<S> getGroupData();

    public abstract List<I> getChildData(int groupPosition);

    public abstract void onBindGroupHolder(GroupHolder holder, int groupPosition);

    public abstract void onBindChildHolder(ChildHolder holder, int groupPosition, int childPosition);


    public class GroupHolder extends BaseViewHolder {

        public GroupHolder(View itemView) {
            super(itemView);
        }
    }

    public class ChildHolder extends BaseViewHolder {

        public ChildHolder(View itemView) {
            super(itemView);
        }
    }
}
